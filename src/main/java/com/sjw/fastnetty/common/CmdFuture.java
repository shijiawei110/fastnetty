package com.sjw.fastnetty.common;

import com.sjw.fastnetty.callback.AsyncCallBack;
import com.sjw.fastnetty.protocol.CmdPackage;
import io.netty.channel.Channel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private SemaphoreOnce semaphoreOnce;
    private AsyncCallBack asyncCallBack;
    private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);

    public CmdFuture(long sn, long timeOutMillis, Channel channel) {
        this.sn = sn;
        this.timeOutMillis = timeOutMillis;
        this.channel = channel;
        startTimeMills = System.currentTimeMillis();
    }

    public CmdFuture(long sn, long timeOutMillis, Channel channel, SemaphoreOnce semaphoreOnce, AsyncCallBack asyncCallBack) {
        this.sn = sn;
        this.timeOutMillis = timeOutMillis;
        this.channel = channel;
        this.semaphoreOnce = semaphoreOnce;
        this.asyncCallBack = asyncCallBack;
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

    public boolean isAysncCallBack() {
        if (null == asyncCallBack) {
            return false;
        }
        return true;
    }

    /**
     * 释放一个限流器(只能被释放一次)
     */
    public void releaseSemaphore() {
        if (this.semaphoreOnce != null) {
            this.semaphoreOnce.release();
        }
    }

    /**
     * 执行回调
     */
    public void executeInvokeCallback(boolean taskSuccessFlag) {
        if (asyncCallBack != null) {
            //防止并发执行回调
            if (this.executeCallbackOnlyOnce.compareAndSet(false, true)) {
                if (taskSuccessFlag) {
                    asyncCallBack.execute(this);
                } else {
                    asyncCallBack.executeFail(this);
                }

            }
        }
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
