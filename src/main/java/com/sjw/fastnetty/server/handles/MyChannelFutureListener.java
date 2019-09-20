package com.sjw.fastnetty.server.handles;//package com.sjw.magi.network.server.handles;
//
//import com.sjw.magi.network.utils.ChannelHelper;
//import io.netty.channel.ChannelFuture;
//import io.netty.util.concurrent.GenericFutureListener;
//
///**
// * @author shijiawei
// * @version MyChannelFutureListener.java, v 0.1
// * @date 2019/1/23
// * 自定义 心跳触发监听
// */
//public interface MyChannelFutureListener extends GenericFutureListener<ChannelFuture> {
//    MyChannelFutureListener CLOSE_REMOTING_CLIENT = new MyChannelFutureListener() {
//        @Override
//        public void operationComplete(ChannelFuture future) {
//            if (!future.isSuccess()) {
//                ChannelHelper.closeChannel(future.channel());
//            }
//        }
//    };
//}
