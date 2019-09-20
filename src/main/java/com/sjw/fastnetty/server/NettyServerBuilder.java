package com.sjw.fastnetty.server;

import com.sjw.fastnetty.nettybase.listener.ChannelEventListener;
import lombok.Data;

/**
 * @author shijiawei
 * @version NettyServerBuilder.java, v 0.1
 * @date 2019/1/22
 */
@Data
public class NettyServerBuilder {
    /**
     * 监听端口
     **/
    private int listenerPort = 10086;

    /**
     * boss线程数
     **/
    private int bossGroupSize = 1;

    /**
     * work线程数
     */
    private int workGroupSize = 2;

    /**
     * 请求超时时间（服务端不设置tcp参数,而是在请求的时候cas使用）
     */
    private long timeOutMills = 5000L;

    /**
     * 是否配置对客户端的心跳监听 <=0 就是不配置 数值就是心跳超时时间(单位秒)
     */
    private long heartBeatSeconds = 0;

    /**
     * 服务端最大连接数
     */
    private int maxChannelNum = 0;

    public NettyServer build() {
        return new NettyServer(this);
    }

    public NettyServer build(ChannelEventListener channelEventListener) {
        return new NettyServer(this, channelEventListener);
    }


}
