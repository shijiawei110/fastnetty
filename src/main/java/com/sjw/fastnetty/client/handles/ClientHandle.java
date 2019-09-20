package com.sjw.fastnetty.client.handles;

import com.google.common.collect.Maps;
import com.sjw.fastnetty.client.SystemClosePolling;
import com.sjw.fastnetty.common.HandleBase;
import com.sjw.fastnetty.enums.SystemRunStatus;
import com.sjw.fastnetty.exception.MagiException;
import com.sjw.fastnetty.nettybase.listener.ListenEvent;
import com.sjw.fastnetty.nettybase.listener.ListenEventType;
import com.sjw.fastnetty.protocol.CmdPackage;
import com.sjw.fastnetty.utils.AqsKeeper;
import com.sjw.fastnetty.utils.ChannelHelper;
import com.sjw.fastnetty.utils.SyncPollingUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.concurrent.ConcurrentMap;

/**
 * @author shijiawei
 * @version ClientHandle.java, v 0.1
 * @date 2019/1/24
 */
@Slf4j
@ChannelHandler.Sharable
public class ClientHandle extends SimpleChannelInboundHandler<CmdPackage> {

    private HandleBase handleBase = new HandleBase();

    private ConcurrentMap<String, Channel> channelTable = Maps.newConcurrentMap();

    private AqsKeeper channelTableKeeper = new AqsKeeper();

    /**
     * client注销标志
     */
    private SystemRunStatus clientRunStatus = SystemRunStatus.RUNNING;

    public HandleBase base() {
        return handleBase;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String addr = ChannelHelper.getRemoteAddr(channel);
        log.info("fastnetty client active a channel -> addr {}", addr);
        if (handleBase.isOpenEventListener()) {
            handleBase.getEventListenerExecutor().addEvent(new ListenEvent(ListenEventType.CONNECT, addr, channel));
        }
    }

    /**
     * 重写连接关闭,当与server断开连接时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String addr = ChannelHelper.getRemoteAddr(channel);
        log.info("fastnetty client inactive a channel -> addr {}", addr);
        if (handleBase.isOpenEventListener()) {
            handleBase.getEventListenerExecutor().addEvent(new ListenEvent(ListenEventType.CLOSE, addr, channel));
        }
        clientCloseChannel(channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        String addr = ChannelHelper.getRemoteAddr(ctx.channel());
        log.error("fastnetty client channel handler happen a exception so close this channel" +
                "-> remoteAddr = {},stack={}", addr, ExceptionUtils.getStackTrace(cause));
        if (handleBase.isOpenEventListener()) {
            handleBase.getEventListenerExecutor().addEvent(new ListenEvent(ListenEventType.EXCEPTION, addr, channel));
        }
        clientCloseChannel(channel);
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, CmdPackage request) {
        Channel channel = ctx.channel();
        String addr = ChannelHelper.getRemoteAddr(channel);
        log.info("fastnetty client receive a netty msg -> req = {},addr ={}", request.toString(), addr);
        switch (request.getCmdType()) {
            case REQ:
                handleBase.doRequest(channel, request);
                break;
            case ONE_WAY_REQ:
                handleBase.doOneWayRequest(channel, request);
                break;
            case RES:
                handleBase.doResponse(channel, request);
                break;
            default:
                log.info("fastnetty client handle receive a illegal cmd -> addr={} , cmdType ={]",
                        addr, request.getCmdType());
                clientCloseChannel(channel);
                break;
        }
    }

    /**
     * 客户端关闭连接 关闭channel的同时 需要在table中剔除该连接和地址
     */
    public void clientCloseChannel(Channel channel) {
        if (null == channel) {
            return;
        }
        boolean getLockFlag = false;
        try {
            if (!channelTableKeeper.tryLock()) {
                throw MagiException.CLIENT_GET_CHANNEL_TABLE_LOCK_OUT_TIME;
            }
            getLockFlag = true;
            channelTable.entrySet().removeIf(entry -> entry.getValue() == channel);

        } catch (Exception e) {
            log.error("fastnetty client delete a channel expection -> server addr={},stack={}",
                    ChannelHelper.getRemoteAddr(channel), ExceptionUtils.getStackTrace(e));

        } finally {
            if (ChannelHelper.alive(channel)) {
                ChannelHelper.closeChannel(channel);
            }
            if (getLockFlag) {
                channelTableKeeper.unlock();
            }
        }
        //进行请求的快速失败
        handleBase.channelCloseFastFail(channel);
    }

    /**
     * 优雅注销channels 等待所有request 请求完再结束注销所有channel
     */
    public void shutdown() {
        //进入ending状态
        clientRunStatus = SystemRunStatus.ENDING;
        //等请求完成再关闭
        SyncPollingUtil.polling(3000L, 30, new SystemClosePolling(handleBase.getCmdContainer()));
        //超时强制关闭
        if (null != channelTable && channelTable.size() > 0) {
            channelTable.forEach((k, v) -> {
                ChannelHelper.closeChannel(v);
            });
        }
    }


    public AqsKeeper getChannelTableKeeper() {
        return channelTableKeeper;
    }


    public ConcurrentMap<String, Channel> getChannelTable() {
        return channelTable;
    }

    public SystemRunStatus getClientRunStatus() {
        return clientRunStatus;
    }

    public void setClientRunStatus(SystemRunStatus clientRunStatus) {
        this.clientRunStatus = clientRunStatus;
    }
}
