package com.sjw.fastnetty.nettybase;

import com.google.common.collect.Maps;
import com.sjw.fastnetty.callback.AsyncCallBack;
import com.sjw.fastnetty.client.SystemClosePolling;
import com.sjw.fastnetty.common.CmdFuture;
import com.sjw.fastnetty.common.HandleBase;
import com.sjw.fastnetty.common.SemaphoreOnce;
import com.sjw.fastnetty.exception.FastNettyException;
import com.sjw.fastnetty.nettybase.listener.ChannelEventListener;
import com.sjw.fastnetty.nettybase.listener.EventListenerExecutor;
import com.sjw.fastnetty.protocol.CmdPackage;
import com.sjw.fastnetty.protocol.ResponseCodeType;
import com.sjw.fastnetty.utils.ChannelHelper;
import com.sjw.fastnetty.utils.SyncPollingUtil;
import com.sjw.fastnetty.utils.SystemUtil;
import com.sjw.fastnetty.utils.ThreadNameUtil;
import io.netty.channel.Channel;
import io.netty.channel.epoll.Epoll;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author shijiawei
 * @version NettyBase.java, v 0.1
 * @date 2019/1/22
 */
@Slf4j
public class NettyBase {
    /**
     * 最短超时时间不能低于这个数字
     **/
    protected static final long MIN_TIME_OUT_MILLS = 100L;
    /**
     * 基础handle执行器
     */
    protected HandleBase handleBase;

    private ChannelEventListener channelEventListener;

    private Thread listenerThread;

    /**
     * 僵尸请求清除任务线程
     */
    private ScheduledExecutorService scanScheduler;

    /**
     * 单向指令限流器
     */
    private Semaphore oneWayLimit = new Semaphore(2048);
    /**
     * 异步指令限流器
     */
    private Semaphore asyncLimit = new Semaphore(2048);

    protected EventListenerExecutor eventListenerExecutor = new EventListenerExecutor();

    protected ConcurrentMap<Long, CmdFuture> cmdContainer = Maps.newConcurrentMap();

    public void setChannelEventListener(ChannelEventListener channelEventListener) {
        this.channelEventListener = channelEventListener;
    }

    protected boolean isEpoll() {
        return SystemUtil.isLinux() && Epoll.isAvailable();
    }

    /**
     * 启动自定义事件监听
     */
    protected void startEventListen() {
        if (null != channelEventListener) {
            eventListenerExecutor.setListener(channelEventListener);
            listenerThread = new Thread(eventListenerExecutor, ThreadNameUtil.getName("event-listen"));
            listenerThread.start();
            log.info("fastnetty server channel event listener start succeed");
        }
    }

    /**
     * 关闭自定义事件监听
     */
    protected void shutdownEventListen() {
        eventListenerExecutor.setStopFlag(true);
    }

    /**
     * 优雅注销的时候 需要把请求指令全部完成
     */
    protected void shutdownWaitCmdComplete() {
        SyncPollingUtil.polling(3000L, 30, new SystemClosePolling(cmdContainer));
    }

    /**
     * 扫描 清除因为网络原因 躺在容器里的 "僵尸" 请求 （用于异步请求,同步请求都会在finally中删除）
     */
    protected void openClearNoResCmd() {
        scanScheduler = Executors.newSingleThreadScheduledExecutor(
                runnable -> new Thread(runnable, ThreadNameUtil.getName("clearCmd-thread")));
        scanScheduler.scheduleAtFixedRate(() -> {
            if (null == cmdContainer) {
                return;
            }
            if (cmdContainer.size() <= 0) {
                return;
            }
            Iterator<Map.Entry<Long, CmdFuture>> it = this.cmdContainer.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, CmdFuture> next = it.next();
                CmdFuture v = next.getValue();
                long dif = System.currentTimeMillis() - v.getStartTimeMills();
                long dis = v.getTimeOutMillis() + 3000;
                if (dif > dis) {
                    log.info("scan cmd container clear a dead requset -> sn={}", next.getKey());
                    it.remove();
                    //执行回调
                    if (v.isAysncCallBack()) {
                        handleBase.executeAsyncCallback(v, false);
                    }
                }
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    protected void closeClearNoResCmd() {
        if (null != scanScheduler) {
            scanScheduler.shutdown();
        }
    }

    /**
     * 同步指令(同步获取结果)
     */
    protected CmdPackage doCmdSync(final Channel channel, final CmdPackage request, final long timeOutMillis) throws InterruptedException {
        String linkAddr = ChannelHelper.getRemoteAddr(channel);
        long sn = request.getSn();
        try {
            CmdFuture cmdFuture = new CmdFuture(sn, timeOutMillis, channel);
            cmdContainer.put(sn, cmdFuture);
            channel.writeAndFlush(request).addListener(future -> {
                if (!future.isSuccess()) {
                    cmdFuture.setSendRequestSuccess(false);
                    cmdContainer.remove(sn);
                    cmdFuture.putResponse(null);
                    log.info("fastnetty sync send a cmd fail -> to addr ={}", linkAddr);
                }
            });

            CmdPackage cmdPackage = cmdFuture.acuireResponse(timeOutMillis);
            if (null == cmdPackage) {
                if (cmdFuture.isSendRequestSuccess()) {
                    //超时
                    throw FastNettyException.SEND_CMD_OUT_TIME;
                } else {
                    //发送失败
                    throw FastNettyException.SEND_CMD_FAIL;
                }
            }
            //如果应答异常 log记录
            if (ResponseCodeType.ERROR == cmdPackage.getResponseCodeType()) {
                log.info("fastnetty system receive a response handle error -> addr ={},errCode={},errMsg={}",
                        linkAddr, cmdPackage.getErrorCode(), cmdPackage.getErrorMsg());
            }
            return cmdPackage;
        } finally {
            cmdContainer.remove(sn);
        }
    }

    /**
     * 单向指令
     */
    protected void doOneWay(final Channel channel, final CmdPackage request, final long timeOutMillis) throws InterruptedException {
        String linkAddr = ChannelHelper.getRemoteAddr(channel);
        boolean acquired = oneWayLimit.tryAcquire(timeOutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            SemaphoreOnce semaphoreOnce = new SemaphoreOnce(oneWayLimit);
            try {
                channel.writeAndFlush(request).addListener(future -> {
                    semaphoreOnce.release();
                    if (!future.isSuccess()) {
                        log.warn("one way request send complete but fail -> addr={} cmd={}",
                                linkAddr, request.toString());
                    }
                });
            } catch (Exception e) {
                //释放1个限流器
                semaphoreOnce.release();
                log.error("one way request sending take a exception -> addr={} cmd={} stack={}",
                        linkAddr, request.toString(), ExceptionUtils.getStackTrace(e));
                throw e;
            }
        } else {
            log.warn("one way request can not get the lock from semaphore -> totalNum={},now waiting thread num={}",
                    oneWayLimit.getQueueLength(),
                    oneWayLimit.availablePermits());
        }
    }

    /**
     * 异步指令
     */
    protected void doCmdAsync(final Channel channel, final CmdPackage request, final long timeOutMillis, final AsyncCallBack asyncCallBack) throws InterruptedException {
        final long sn = request.getSn();
        String linkAddr = ChannelHelper.getRemoteAddr(channel);
        boolean acquired = asyncLimit.tryAcquire(timeOutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            SemaphoreOnce semaphoreOnce = new SemaphoreOnce(asyncLimit);
            CmdFuture cmdFuture = new CmdFuture(sn, timeOutMillis, channel, semaphoreOnce, asyncCallBack);
            cmdContainer.put(sn, cmdFuture);
            try {
                channel.writeAndFlush(request).addListener(future -> {
                    if (!future.isSuccess()) {
                        handleBase.requestClose(sn);
                        log.warn("async request send complete but fail -> addr={} cmd={}",
                                linkAddr, request.toString());
                    }
                });
            } catch (Exception e) {
                handleBase.requestClose(sn);
                log.error("async request sending take a exception -> addr={} cmd={} stack={}",
                        linkAddr, request.toString(), ExceptionUtils.getStackTrace(e));
                throw e;
            }
        } else {
            log.error("async request out of limit -> available : {}, all : {}", asyncLimit.availablePermits(), asyncLimit.getQueueLength());
        }
    }


}
