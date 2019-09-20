package com.sjw.fastnetty.common;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shijiawei
 * @version SemaphoreOnce.java -> v 1.0
 * @date 2019/4/9
 * 保证限流器不会出现并发释放多次释放 的问题
 */
public class SemaphoreOnce {
    private final AtomicBoolean released = new AtomicBoolean(false);
    private final Semaphore semaphore;

    public SemaphoreOnce(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void release() {
        if (this.semaphore != null) {
            if (this.released.compareAndSet(false, true)) {
                this.semaphore.release();
            }
        }
    }
}
