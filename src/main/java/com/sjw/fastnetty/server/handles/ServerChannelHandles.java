package com.sjw.fastnetty.server.handles;

import com.sjw.fastnetty.common.CmdFuture;
import com.sjw.fastnetty.common.TcpPackageHander;
import com.sjw.fastnetty.nettybase.listener.EventListenerExecutor;
import com.sjw.fastnetty.protocol.MagiDecoder;
import com.sjw.fastnetty.protocol.MagiEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author shijiawei
 * @version ServerChannelHandles.java, v 0.1
 * @date 2019/1/24
 */
public class ServerChannelHandles extends ChannelInitializer<SocketChannel> {

    private ServerHandle serverHandle;

    private long heartBeatSeconds;

    public ServerChannelHandles(ConcurrentMap<Long, CmdFuture> cmdContainer, boolean isOpenEventListen,
                                EventListenerExecutor eventListenerExecutor, long heartBeatSeconds,
                                int maxChannelNum) {
        if (null == serverHandle) {
            serverHandle = new ServerHandle(maxChannelNum);
            serverHandle.base().setCmdContainer(cmdContainer);
            //如果开启了自定义事件监听
            if (isOpenEventListen) {
                serverHandle.base().setOpenEventListener(true);
                serverHandle.base().setEventListenerExecutor(eventListenerExecutor);
            }
            //设置心跳监听配置时间
            this.heartBeatSeconds = heartBeatSeconds;
        }
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        /**
         * 管道的各个处理器
         * 1:粘拆包处理器
         * 2：编码器
         * 3：解码器
         * 4:心跳
         * 5:心跳监听
         * 6:业务处理器
         */
        pipeline.addLast(new TcpPackageHander());
        pipeline.addLast(MagiEncoder.getInstance());
        pipeline.addLast(new MagiDecoder());
        if (heartBeatSeconds > 0) {
            //n秒内不管读写 没有检测到心跳,就执行监听事件再次check client是否存活
            pipeline.addLast(new IdleStateHandler(0, 0, heartBeatSeconds, TimeUnit.SECONDS));
            pipeline.addLast(new HeartBeatHandler());
        }
        pipeline.addLast(serverHandle);
    }

    public ServerHandle getServerHandle() {
        return serverHandle;
    }
}
