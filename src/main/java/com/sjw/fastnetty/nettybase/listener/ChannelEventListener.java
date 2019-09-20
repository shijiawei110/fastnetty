package com.sjw.fastnetty.nettybase.listener;

import io.netty.channel.Channel;

/**
 * @author shijiawei
 * @version ChannelEventListener.java, v 0.1
 * @date 2019/1/22
 * 连接时间 处理 接口
 */
public interface ChannelEventListener {
    /**
     * 连接开启触发监听
     * @param linkAddress
     * @param channel
     */
    void onChannelConnect(String linkAddress, Channel channel);

    /**
     * 连接关闭触发监听
     * @param linkAddress
     * @param channel
     */
    void onChannelClose(String linkAddress, Channel channel);

    /**
     * 连接异常触发监听
     * @param linkAddress
     * @param channel
     */
    void onChannelException(String linkAddress, Channel channel);

//    /**
//     * 心跳 触发监听
//     * @param linkAddress
//     * @param channel
//     */
//    void onChannelIdle(String linkAddress, Channel channel);
}
