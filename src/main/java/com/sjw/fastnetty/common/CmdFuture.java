package com.sjw.fastnetty.common;

import com.sjw.fastnetty.protocol.CmdPackage;
import io.netty.channel.Channel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author shijiawei
 * @version CmdFuture.java, v 0.1
 * @date 2019/1/25
 */
public class CmdFuture {
    private final long sn;
    private final long timeOutMillis;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private volatile CmdPackage response;
    private volatile boolean sendRequestSuccess = true;
    private long startTimeMills;
    private Channel channel;

    public CmdFuture(long sn, long timeOutMillis, Channel channel) {
        this.sn = sn;
        this.timeOutMillis = timeOutMillis;
        this.channel = channel;
        startTimeMills = System.currentTimeMillis();
    }

    public CmdPackage acuireResponse(final long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.response;
    }

    public void putResponse(final CmdPackage response) {
        this.response = response;
        this.countDownLatch.countDown();
    }

    public boolean isSendRequestSuccess() {
        return sendRequestSuccess;
    }

    public void setSendRequestSuccess(boolean sendRequestSuccess) {
        this.sendRequestSuccess = sendRequestSuccess;
    }

    public long getStartTimeMills() {
        return startTimeMills;
    }

    public long getTimeOutMillis() {
        return timeOutMillis;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
