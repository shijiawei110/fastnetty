package com.sjw.fastnetty.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * @author shijiawei
 * @version ChannelHelper.java, v 0.1
 * @date 2019/1/24
 * 连接管理帮助工具
 */
@Slf4j
public class ChannelHelper {

    public static String getRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }

        return "";
    }

    public static void closeChannel(Channel channel) {
        if (null == channel) {
            return;
        }
        if (channel.isActive()) {
            final String addrRemote = getRemoteAddr(channel);
            channel.close().addListener(
                    (ChannelFutureListener) future -> log.info("fastnetty close a channel -> remote address[{}] isSuccess: {}", addrRemote, future.isSuccess())
            );
        }
    }

    public static boolean alive(Channel channel) {
        if (null == channel) {
            return false;
        }
        return channel.isActive();
    }

    public static boolean isEquals(Channel channelOne, Channel channelTwo) {
        if (null == channelOne || null == channelTwo) {
            return false;
        }
        if (channelOne == channelTwo) {
            return true;
        }
        return false;
    }


}
