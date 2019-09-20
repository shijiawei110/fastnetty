package com.sjw.fastnetty.server.handles;

import com.google.common.collect.Sets;
import com.sjw.fastnetty.common.HandleBase;
import com.sjw.fastnetty.nettybase.listener.ListenEvent;
import com.sjw.fastnetty.nettybase.listener.ListenEventType;
import com.sjw.fastnetty.protocol.CmdPackage;
import com.sjw.fastnetty.utils.ChannelHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Set;

/**
 * @author shijiawei
 * @version ServerHandle.java, v 0.1
 * @date 2019/1/24
 */
@Slf4j
@ChannelHandler.Sharable
public class ServerHandle extends SimpleChannelInboundHandler<CmdPackage> {

    private static final int MIN_CHANNEL_NUM = 100;

    private HandleBase handleBase = new HandleBase();

    private int maxChannelNum;

    //记录连接地址控制连接数
    private Set<String> channelAddrSet = Sets.newHashSet();

    public ServerHandle(int maxChannelNum) {
        maxChannelNum = maxChannelNum < MIN_CHANNEL_NUM ? MIN_CHANNEL_NUM : maxChannelNum;
        this.maxChannelNum = maxChannelNum;
        log.info("fastnetty server new a server handle");
    }

    public HandleBase base() {
        return handleBase;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String addr = ChannelHelper.getRemoteAddr(channel);
        //到达最大连接数就直接关闭连接
        int currentChannelNumInt = channelAddrSet.size();
        if (currentChannelNumInt > maxChannelNum) {
            log.error("fastnetty server reach limit channel num , close the channel -> addr = {}, currentChannelNum = {}, maxChannelNum",
                    addr, currentChannelNumInt, maxChannelNum);
            ChannelHelper.closeChannel(channel);
        }
        log.info("fastnetty server active a channel -> addr : {}", addr);
        if (handleBase.isOpenEventListener()) {
            handleBase.getEventListenerExecutor().addEvent(new ListenEvent(ListenEventType.CONNECT, addr, channel));
        }
        //加入连接表
        addChannel(addr);
    }

    /**
     * 重写连接关闭,当与server断开连接时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String addr = ChannelHelper.getRemoteAddr(channel);
        log.info("fastnetty server inactive a channel -> addr {}", addr);
        if (handleBase.isOpenEventListener()) {
            handleBase.getEventListenerExecutor().addEvent(new ListenEvent(ListenEventType.CLOSE, addr, channel));
        }
        ChannelHelper.closeChannel(channel);
        delChannel(addr);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        String addr = ChannelHelper.getRemoteAddr(ctx.channel());
        log.error("fastnetty server channel handler happen a exception so magi close this channel" +
                "-> remoteAddr = {},stack={}", addr, ExceptionUtils.getStackTrace(cause));
        if (handleBase.isOpenEventListener()) {
            handleBase.getEventListenerExecutor().addEvent(new ListenEvent(ListenEventType.EXCEPTION, addr, channel));
        }
        ChannelHelper.closeChannel(channel);
        delChannel(addr);
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, CmdPackage request) {
        Channel channel = ctx.channel();
        String addr = ChannelHelper.getRemoteAddr(channel);
        log.info("fastnetty server receive a netty msg -> req = {},addr ={}", request.toString(), addr);
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
                log.info("fastnetty server server handle receive a illegal cmd -> addr={} , cmdType ={]",
                        addr, request.getCmdType());
                ChannelHelper.closeChannel(channel);
                if (handleBase.isOpenEventListener()) {
                    handleBase.getEventListenerExecutor().addEvent(new ListenEvent(ListenEventType.EXCEPTION, addr, channel));
                }
                break;
        }
    }


    private void addChannel(String channelAddr) {
        if (StringUtils.isBlank(channelAddr)) {
            return;
        }
        channelAddrSet.add(channelAddr);
    }

    private synchronized void delChannel(String channelAddr) {
        if (StringUtils.isBlank(channelAddr)) {
            return;
        }
        channelAddrSet.remove(channelAddr);
    }


}
