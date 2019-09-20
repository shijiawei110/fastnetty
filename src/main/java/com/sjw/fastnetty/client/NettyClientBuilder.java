package com.sjw.fastnetty.client;

import com.sjw.fastnetty.nettybase.listener.ChannelEventListener;
import lombok.Data;

/**
 * @author shijiawei
 * @version NettyClientBuilder.java, v 0.1
 * @date 2019/1/22
 */
@Data
public class NettyClientBuilder {
    /**
     * server地址 (之后改成list)
     */

    private String serverHost = "127.0.0.1";
    private int serverPort = 10086;
    /**
     * work线程数
     */
    private int workGroupSize = 2;

    /**
     * 超时毫秒数
     */
    private long outTimeMills = 5000L;


    public NettyClient build() {
        return new NettyClient(this);
    }

    public NettyClient build(ChannelEventListener channelEventListener) {
        return new NettyClient(this, channelEventListener);
    }
}
