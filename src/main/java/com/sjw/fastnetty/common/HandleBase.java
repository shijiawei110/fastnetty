package com.sjw.fastnetty.common;

import com.google.common.collect.Maps;
import com.sjw.fastnetty.nettybase.listener.EventListenerExecutor;
import com.sjw.fastnetty.protocol.CmdPackage;
import com.sjw.fastnetty.utils.ChannelHelper;
import com.sjw.fastnetty.utils.ThreadPoolUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author shijiawei
 * @version HandleBase.java, v 0.1
 * @date 2019/2/24
 */
@Slf4j
public class HandleBase {


    private ConcurrentMap<Long, CmdFuture> cmdContainer;

    private EventListenerExecutor eventListenerExecutor;

    private boolean isOpenEventListener = false;

    private Map<Integer, ReqCmdProcessorHolder> reqCmdProcessorHolders = Maps.newHashMap();

    private RequestBefore requestBefore;

    private RequestAfter requestAfter;

    private ExecutorService asyncCallbackPool = ThreadPoolUtil.createCustomPool("async-call", 1, 50);


    /**
     * 处理client请求
     * 1: 首先寻找注册的处理器
     * 2：如果没有匹配的处理器 那么就回复错误信息，每个处理器自己带一个线程池去接worker线程的任务 也是就是(boss->worker->自己的线程池的 三级处理模式)
     * 3：执行aop前置(如果有)
     * 4：调用处理器处理
     * 5：执行aop后置(如果有)
     * 6: 判断是否为单向通信模式,是的话不予以回复
     * error -> 返回一个系统错误应答
     */
    public void doRequest(Channel channel, CmdPackage request) {
        long sn = request.getSn();
        // -> 1
        Integer code = request.getCmdCode();
        ReqCmdProcessorHolder processorHolder = reqCmdProcessorHolders.get(code);
        // -> 2
        if (null == processorHolder) {
            ReqCmdError reqCmdError = ReqCmdError.NO_PROCESSOR;
            CmdPackage response = CmdPackage.errorRes(sn, reqCmdError);
            channel.writeAndFlush(response);
            return;
        }
        ReqCmdProcessor processor = processorHolder.getReqCmdProcessor();
        ExecutorService taskPool = processorHolder.getExecutorService();
        // -> 3,4,5,6,error  |  async
        ReqCmdProcessorTask task = new ReqCmdProcessorTask(request, channel, processor, requestBefore, requestAfter);
        try {
            taskPool.submit(task);
        } catch (RejectedExecutionException e) {
            //线程队列打满
            if (!request.isOneWay()) {
                ReqCmdError reqCmdError = ReqCmdError.SYSTEM_BUSY;
                CmdPackage response = CmdPackage.errorRes(sn, reqCmdError);
                channel.writeAndFlush(response);
            }
            log.error("fastnetty server processor request task pool busy -> addr={}", ChannelHelper.getRemoteAddr(channel));
        }

    }

    /**
     * 处理client的应答(也就是收到自己发给client的请求的回复)
     */
    public void doResponse(Channel channel, CmdPackage cmdPackage) {
        String addr = ChannelHelper.getRemoteAddr(channel);
        final long sn = cmdPackage.getSn();
        final CmdFuture cmdFuture = cmdContainer.get(sn);
        if (null == cmdFuture) {
            //没有匹配的请求
            log.info("fastnetty server received a response but not find match request -> addr={} sn={}", addr, sn);
            return;
        }
        cmdContainer.remove(sn);
        //异步请求且有回调
        if (cmdFuture.isAysncCallBack()) {
            executeAsyncCallback(cmdFuture, true);
        }
        //同步请求或者没有回调的异步请求
        else {
            cmdFuture.putResponse(cmdPackage);
            //因为有可能没有是配置回调的异步请求,也要释放限流令牌
            cmdFuture.releaseSemaphore();
        }

    }

    /**
     * 处理单向请求
     */
    public void doOneWayRequest(Channel channel, CmdPackage cmdPackage) {
        //啥事不做 可以打个日志
    }

    /**
     * 注册处理器
     */
    public void registerReqCmdProcessor(int code, ReqCmdProcessorHolder holder) {
        reqCmdProcessorHolders.put(code, holder);
    }


    public void setEventListenerExecutor(EventListenerExecutor eventListenerExecutor) {
        this.eventListenerExecutor = eventListenerExecutor;
    }

    public void setOpenEventListener(boolean openEventListener) {
        isOpenEventListener = openEventListener;
    }

    public void setCmdContainer(ConcurrentMap<Long, CmdFuture> cmdContainer) {
        this.cmdContainer = cmdContainer;
    }

    public ConcurrentMap<Long, CmdFuture> getCmdContainer() {
        return cmdContainer;
    }

    public void setRequestBefore(RequestBefore requestBefore) {
        this.requestBefore = requestBefore;
    }

    public void setRequestAfter(RequestAfter requestAfter) {
        this.requestAfter = requestAfter;
    }

    public boolean isOpenEventListener() {
        return isOpenEventListener;
    }

    public EventListenerExecutor getEventListenerExecutor() {
        return eventListenerExecutor;
    }

    /**
     * 销毁所有处理器线程池
     */
    public void closeAllProcessor() {
        reqCmdProcessorHolders.forEach((k, v) -> {
            ExecutorService pool = v.getExecutorService();
            if (null != pool) {
                pool.shutdown();
            }
        });
    }

    /**
     * 快速失败机制 , channel 关闭的时候快速失败掉请求,不让他超时才被清除
     */
    public void channelCloseFastFail(Channel channel) {
        Iterator<Map.Entry<Long, CmdFuture>> it = this.cmdContainer.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, CmdFuture> next = it.next();
            CmdFuture v = next.getValue();
            if (null == v) {
                continue;
            }
            Channel currentChannel = v.getChannel();
            if (null == currentChannel) {
                continue;
            }
            if (currentChannel == channel) {
                //match 到请求 关闭这个request
                requestClose(next.getKey());
            }
        }
    }

    /**
     * 请求被关闭
     */
    public void requestClose(long sn) {
        CmdFuture cmdFuture = cmdContainer.remove(sn);
        //异步指令关闭操作
        if (cmdFuture != null) {
            cmdFuture.setSendRequestSuccess(false);
            cmdFuture.putResponse(null);
            try {
                if (cmdFuture.isAysncCallBack()) {
                    executeAsyncCallback(cmdFuture, false);
                }
            } catch (Throwable e) {
                log.warn("execute callback in request close, and callback throw", e);
            } finally {
                cmdFuture.releaseSemaphore();
            }
        }
    }

    /**
     * 执行异步请求callback todo: 三个地方使用 callback需要有一个成功回调和一个失败回调,在清除僵尸请求的时候执行失败回调
     */
    public void executeAsyncCallback(CmdFuture cmdFuture, boolean taskSuccessFlag) {
        try {
            asyncCallbackPool.execute(() -> doExecuteAsyncCallback(cmdFuture, taskSuccessFlag));
        } catch (RejectedExecutionException e) {
            log.error("async callback pool is full so reject request!");
        }

    }

    private void doExecuteAsyncCallback(CmdFuture cmdFuture, boolean taskSuccessFlag) {
        try {
            cmdFuture.executeInvokeCallback(taskSuccessFlag);
        } catch (Exception e) {
            log.error("do execute async callback error", e);
        } finally {
            cmdFuture.releaseSemaphore();
        }
    }


}
