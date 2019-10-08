package com.sjw.fastnetty.client;

import com.sjw.fastnetty.callback.AsyncCallBack;
import com.sjw.fastnetty.client.handles.ClientChannelHandles;
import com.sjw.fastnetty.common.ReqCmdProcessorHolder;
import com.sjw.fastnetty.common.RequestAfter;
import com.sjw.fastnetty.common.RequestBefore;
import com.sjw.fastnetty.enums.SystemRunStatus;
import com.sjw.fastnetty.exception.FastNettyException;
import com.sjw.fastnetty.nettybase.NettyBase;
import com.sjw.fastnetty.nettybase.listener.ChannelEventListener;
import com.sjw.fastnetty.nettybase.service.NetWorkClient;
import com.sjw.fastnetty.protocol.CmdPackage;
import com.sjw.fastnetty.utils.ChannelHelper;
import com.sjw.fastnetty.utils.IpUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * @author shijiawei
 * @version NettyClient.java, v 0.1
 * @date 2019/1/22
 * netty 客户端服务
 */
@Slf4j
public class NettyClient extends NettyBase implements NetWorkClient {

    private NettyClientBuilder nettyClientBuilder;

    private Bootstrap bootstrap;

    private EventLoopGroup workerGroup;

    private ClientChannelHandles clientChannelHandles;

    private boolean isOpenEventListen = false;

    public NettyClient(NettyClientBuilder nettyClientBuilder) {
        this(nettyClientBuilder, null);
    }

    public NettyClient(NettyClientBuilder nettyClientBuilder, ChannelEventListener channelEventListener) {
        this.nettyClientBuilder = nettyClientBuilder;
        //校验超时时间不能太短
        if (nettyClientBuilder.getOutTimeMills() <= MIN_TIME_OUT_MILLS) {
            nettyClientBuilder.setOutTimeMills(MIN_TIME_OUT_MILLS);
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
        bootstrap = new Bootstrap();

        if (isEpoll()) {
            workerGroup = new EpollEventLoopGroup(nettyClientBuilder.getWorkGroupSize());
            bootstrap.channel(EpollSocketChannel.class);
        } else {
            workerGroup = new NioEventLoopGroup(nettyClientBuilder.getWorkGroupSize());
            bootstrap.channel(NioSocketChannel.class);
        }

        bootstrap.group(workerGroup);
        clientChannelHandles = new ClientChannelHandles(cmdContainer, isOpenEventListen, eventListenerExecutor);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) nettyClientBuilder.getOutTimeMills())
                .handler(clientChannelHandles);

        //设置base的handle base
        handleBase = clientChannelHandles.getClientHandle().base();

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
        //首先注销所有与server的channel连接
        clientChannelHandles.getClientHandle().shutdown();

        if (null != workerGroup) {
            workerGroup.shutdownGracefully();
        }
        //关闭监听线程
        shutdownEventListen();
        //关闭僵尸扫描线程池
        closeClearNoResCmd();
        //关闭所有处理器线程池
        clientChannelHandles.getClientHandle().base().closeAllProcessor();
    }


    @Override
    public CmdPackage cmdSync(final String address, final CmdPackage cmd) throws InterruptedException {
        Channel channel = safeGetChannel(address);
        if (ChannelHelper.alive(channel)) {
            CmdPackage response = doCmdSync(channel, cmd, nettyClientBuilder.getOutTimeMills());
            return response;
        } else {
            throw FastNettyException.CLIENT_GET_CHANNEL_ERROR;
        }
    }

    @Override
    public void cmdAsync(final String address, final CmdPackage cmd, final AsyncCallBack asyncCallBack) throws InterruptedException {
        Channel channel = safeGetChannel(address);
        if (ChannelHelper.alive(channel)) {
            doCmdAsync(channel, cmd, nettyClientBuilder.getOutTimeMills(),asyncCallBack);
        } else {
            throw FastNettyException.CLIENT_GET_CHANNEL_ERROR;
        }
    }


    @Override
    public void cmdOneWay(String address, CmdPackage cmd) throws InterruptedException {
        Channel channel = safeGetChannel(address);
        if (ChannelHelper.alive(channel)) {
            doOneWay(channel, cmd, nettyClientBuilder.getOutTimeMills());
        } else {
            throw FastNettyException.CLIENT_GET_CHANNEL_ERROR;
        }
    }


    @Override
    public void registerCmdProcessor(Integer code, ReqCmdProcessorHolder holder) {
        if (null == holder.getExecutorService()) {
            throw FastNettyException.PARAMS_ERROR;
        }
        clientChannelHandles.getClientHandle().base().registerReqCmdProcessor(code, holder);
    }

    @Override
    public void registerRequestBefore(RequestBefore requestBefore) {
        clientChannelHandles.getClientHandle().base().setRequestBefore(requestBefore);
    }

    @Override
    public void registerRequestAfter(RequestAfter requestAfter) {
        clientChannelHandles.getClientHandle().base().setRequestAfter(requestAfter);
    }

    /**
     * 校验系统运行状态
     * 安全获取channel
     *
     * @return
     */
    private Channel safeGetChannel(final String address) throws InterruptedException {
        //校验系统运行状态
        if (!checkSystemStatus()) {
            throw FastNettyException.CLIENT_SYSYTEM_ENDING;
        }
//        long start = System.currentTimeMillis();
        //先要获取channel  再调用同步请求
        Channel channel = getChannel(address);
        //检测获取连接时间,超过超时时间直接关闭连接
//        if (System.currentTimeMillis() - start > nettyClientBuilder.getOutTimeMills()) {
//            clientChannelHandles.getClientHandle().clientCloseChannel(channel);
//        }
        return channel;
    }

    private Channel getChannel(String address) throws InterruptedException {
        if (StringUtils.isBlank(address)) {
            throw FastNettyException.PARAMS_ERROR;
        }
        Channel c = ctGet(address);
        if (ChannelHelper.alive(c)) {
            return c;
        }
        return createChannel(address);
    }

    private Channel createChannel(String address) throws InterruptedException {
        boolean getLockFlag = false;
        try {
            if (!clientChannelHandles.getClientHandle().getChannelTableKeeper().tryLock()) {
                throw FastNettyException.CLIENT_GET_CHANNEL_TABLE_LOCK_OUT_TIME;
            }
            getLockFlag = true;
            Channel channel = ctGet(address);
            if (ChannelHelper.alive(channel)) {
                return channel;
            }
            ChannelFuture channelFuture = bootstrap.connect(IpUtil.string2SocketAddress(address)).sync();
            if (channelFuture.isSuccess()) {
                Channel newChannel = channelFuture.channel();
                ctPut(address, newChannel);
                return newChannel;
            }
            return null;
        } catch (Exception e) {
            log.error("fastnetty client create a channel fail -> server host={},port={},stack={}",
                    nettyClientBuilder.getServerHost(), nettyClientBuilder.getServerPort(), ExceptionUtils.getStackTrace(e));
            return null;
        } finally {
            if (getLockFlag) {
                clientChannelHandles.getClientHandle().getChannelTableKeeper().unlock();
            }
        }
    }

    private Channel ctGet(String address) {
        return clientChannelHandles.getClientHandle().getChannelTable().get(address);
    }

    private void ctPut(String address, Channel channel) {
        clientChannelHandles.getClientHandle().getChannelTable().put(address, channel);
    }

    /**
     * 确认系统状态
     */
    private boolean checkSystemStatus() {
        if (clientChannelHandles.getClientHandle().getClientRunStatus() != SystemRunStatus.RUNNING) {
            return false;
        }
        return true;
    }

}
