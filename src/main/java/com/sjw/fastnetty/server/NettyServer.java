package com.sjw.fastnetty.server;

import com.sjw.fastnetty.common.ReqCmdProcessorHolder;
import com.sjw.fastnetty.common.RequestAfter;
import com.sjw.fastnetty.common.RequestBefore;
import com.sjw.fastnetty.exception.FastNettyException;
import com.sjw.fastnetty.nettybase.NettyBase;
import com.sjw.fastnetty.nettybase.listener.ChannelEventListener;
import com.sjw.fastnetty.nettybase.service.NetWorkServer;
import com.sjw.fastnetty.protocol.CmdPackage;
import com.sjw.fastnetty.server.handles.ServerChannelHandles;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author shijiawei
 * @version NettyServer.java, v 0.1
 * @date 2019/1/22
 * netty 服务端服务
 */
@Slf4j
public class NettyServer extends NettyBase implements NetWorkServer {

    private NettyServerBuilder nettyServerBuilder;

    private ServerBootstrap bootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private ServerChannelHandles serverChannelHandles;

    private boolean isOpenEventListen = false;

    public NettyServer(NettyServerBuilder nettyServerBuilder) {
        this(nettyServerBuilder, null);
    }

    public NettyServer(NettyServerBuilder nettyServerBuilder, ChannelEventListener channelEventListener) {
        this.nettyServerBuilder = nettyServerBuilder;
        //校验超时时间不能太短
        if (nettyServerBuilder.getTimeOutMills() <= MIN_TIME_OUT_MILLS) {
            nettyServerBuilder.setTimeOutMills(MIN_TIME_OUT_MILLS);
        }
        if (null != channelEventListener) {
            setChannelEventListener(channelEventListener);
            isOpenEventListen = true;
        }
    }

    /**
     * 启动服务入口
     */
    @Override
    public void start() {
        //首先注册netty服务
        bootstrap = new ServerBootstrap();

        if (isEpoll()) {
            bossGroup = new EpollEventLoopGroup(nettyServerBuilder.getBossGroupSize());
            workerGroup = new EpollEventLoopGroup(nettyServerBuilder.getWorkGroupSize());
            bootstrap.channel(EpollServerSocketChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(nettyServerBuilder.getBossGroupSize());
            workerGroup = new NioEventLoopGroup(nettyServerBuilder.getWorkGroupSize());
            bootstrap.channel(NioServerSocketChannel.class);
        }

        bootstrap.group(bossGroup, workerGroup);

        serverChannelHandles = new ServerChannelHandles(cmdContainer, isOpenEventListen, eventListenerExecutor,
                nettyServerBuilder.getHeartBeatSeconds(), nettyServerBuilder.getMaxChannelNum());
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(new InetSocketAddress(nettyServerBuilder.getListenerPort()))
                .childHandler(serverChannelHandles);

        //开启服务
        try {
            ChannelFuture sync = bootstrap.bind().sync();
            log.info("fastnetty netty server start succeed! listener port ={}", nettyServerBuilder.getListenerPort());
        } catch (InterruptedException e) {
            throw new RuntimeException("fastnetty netty server start InterruptedException", e);
        }

        //设置base的handle base
        handleBase = serverChannelHandles.getServerHandle().base();

        //启动自定义事件监听
        startEventListen();

        //定时清除僵尸请求
        openClearNoResCmd();
    }


    /**
     * 优雅地关闭服务
     */
    @Override
    public void shutdown() {
        //安全注销过程 需要先把请求都处理完 轮询cmd容器直到全部完成或者超时
        shutdownWaitCmdComplete();

        if (null != bossGroup) {
            bossGroup.shutdownGracefully();
        }
        if (null != workerGroup) {
            workerGroup.shutdownGracefully();
        }
        //关闭监听线程
        shutdownEventListen();
        //关闭僵尸扫描线程池
        closeClearNoResCmd();
        //关闭所有处理器线程池
        serverChannelHandles.getServerHandle().base().closeAllProcessor();
    }

    @Override
    public CmdPackage cmdSync(final Channel channel, final CmdPackage cmd) throws InterruptedException {
        return doCmdSync(channel, cmd, nettyServerBuilder.getTimeOutMills());
    }

    @Override
    public void registerCmdProcessor(Integer code, ReqCmdProcessorHolder holder) {
        if (null == holder.getExecutorService()) {
            throw FastNettyException.PARAMS_ERROR;
        }
        serverChannelHandles.getServerHandle().base().registerReqCmdProcessor(code, holder);
    }

    @Override
    public void registerRequestBefore(RequestBefore requestBefore) {
        serverChannelHandles.getServerHandle().base().setRequestBefore(requestBefore);
    }

    @Override
    public void registerRequestAfter(RequestAfter requestAfter) {
        serverChannelHandles.getServerHandle().base().setRequestAfter(requestAfter);
    }

}
