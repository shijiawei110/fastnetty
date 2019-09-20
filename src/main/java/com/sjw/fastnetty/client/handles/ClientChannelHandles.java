package com.sjw.fastnetty.client.handles;

import com.sjw.fastnetty.common.CmdFuture;
import com.sjw.fastnetty.common.TcpPackageHander;
import com.sjw.fastnetty.nettybase.listener.EventListenerExecutor;
import com.sjw.fastnetty.protocol.MagiDecoder;
import com.sjw.fastnetty.protocol.MagiEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.ConcurrentMap;

/**
 * @author shijiawei
 * @version ServerChannelHandles.java, v 0.1
 * @date 2019/1/24
 */
public class ClientChannelHandles extends ChannelInitializer<SocketChannel> {

    private ClientHandle clientHandle;

    public ClientChannelHandles(ConcurrentMap<Long, CmdFuture> cmdContainer, boolean isOpenEventListener, EventListenerExecutor eventListenerExecutor) {
        if (null == clientHandle) {
            clientHandle = new ClientHandle();
            clientHandle.base().setCmdContainer(cmdContainer);
            //如果开启了自定义事件监听
            if (isOpenEventListener) {
                clientHandle.base().setOpenEventListener(true);
                clientHandle.base().setEventListenerExecutor(eventListenerExecutor);
            }
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
         * 4:业务处理器
         */
        pipeline.addLast(new TcpPackageHander());
        pipeline.addLast(MagiEncoder.getInstance());
        pipeline.addLast(new MagiDecoder());
        pipeline.addLast(clientHandle);
    }

    public ClientHandle getClientHandle() {
        return clientHandle;
    }


}
