package com.sjw.fastnetty.server.handles;

import com.sjw.fastnetty.utils.ChannelHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shijiawei
 * @version HeartBeatHandler.java, v 0.1
 * @date 2019/1/23
 * 心跳 检测处理
 */

@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            //写入心跳包 并且监听
//            log.info("magi system server trigger a heart beat event so we will do a check to client");
//            CmdPackage request = CmdPackage.createReq(ReqCmdProcessorCodeConstant.SERVER_TRIGGER_TRY,null);
//            ctx.writeAndFlush(request).addListener(MyChannelFutureListener.CLOSE_REMOTING_CLIENT);
            Channel channel = ctx.channel();
            log.info("magi system take a break heart beat so we close the channel -> addr={}", ChannelHelper.getRemoteAddr(channel));
            ChannelHelper.closeChannel(channel);
        } else {
            //不是心跳类型 继续向下游传递
            super.userEventTriggered(ctx, evt);
        }
    }
}
