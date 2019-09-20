package com.sjw.fastnetty.nettybase.listener;

import io.netty.channel.Channel;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author shijiawei
 * @version ListenEvent.java, v 0.1
 * @date 2019/1/22
 * 监听事件
 */
public class ListenEvent {
    private ListenEventType listenEventType;
    private String linkAddress;
    private Channel channel;

    public ListenEvent(ListenEventType listenEventType, String linkAddress, Channel channel) {
        this.listenEventType = listenEventType;
        this.linkAddress = linkAddress;
        this.channel = channel;
    }

    public ListenEventType getType() {
        return listenEventType;
    }

    public String getLinkAddress() {
        return linkAddress;
    }

    public Channel getChannel() {
        return channel;
    }


    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
