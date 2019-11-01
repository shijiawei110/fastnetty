package com.sjw.fastnetty.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @author shijiawei
 * @version AqsKeeper.java, v 0.1
 * @date 2018/9/17
 */
public class AqsKeeper {
    private static final int LOCK = 1;
    private static final int UNLOCK = 0;

    private static final long DEFAULT_OUT_TIME = 3000L;

    private AqsSync aqsSync = new AqsSync();


    public AqsKeeper() {
    }

    /**
     * 自旋获取锁
     */
    public void lock() {
        aqsSync.acquire(LOCK);
    }

    /**
     * 可中断和可超时 获取锁
     */
    public boolean tryLock(long outTimeMills) throws InterruptedException {
        return aqsSync.tryAcquireNanos(LOCK, TimeUnit.MILLISECONDS.toNanos(outTimeMills));
    }

    public boolean tryLock() throws InterruptedException {
        return aqsSync.tryAcquireNanos(LOCK, TimeUnit.MILLISECONDS.toNanos(DEFAULT_OUT_TIME));
    }


    /**
     * 释放锁
     */

    public void unlock() {
        aqsSync.tryRelease(UNLOCK);
    }


    /**
     * 内部aqs模板继承类
     */
    private static class AqsSync extends AbstractQueuedSynchronizer {

        /**
         * 判断是否处于lock状态
         *
         * @return
         */
        @Override
        protected boolean isHeldExclusively() {
            return getState() == LOCK;
        }

        @Override
        protected boolean tryAcquire(int acquires) {
            if (compareAndSetState(UNLOCK, LOCK)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;

        }

        @Override
        protected boolean tryRelease(int releases) {
            if (getState() == UNLOCK) {
                throw new IllegalMonitorStateException();
            }
            //没有线程拥有这个锁
            setExclusiveOwnerThread(null);
            setState(UNLOCK);
            return true;
        }
    }


}
