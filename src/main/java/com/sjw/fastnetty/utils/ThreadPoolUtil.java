package com.sjw.fastnetty.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author shijiawei
 * @version ThreadPoolUtil.java, v 0.1
 * @date 2019/2/22
 * 线程池工具类
 */
public class ThreadPoolUtil {

    private static int DEFAULT_CORE_SIZE = 2;
    private static int DEFAULT_MAX_SIZE = 20;

    public static ExecutorService createDefaultPool(String nameTag) {
        ExecutorService pool = new ThreadPoolExecutor(DEFAULT_CORE_SIZE, DEFAULT_MAX_SIZE, 30L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(2048),
                new NamedThreadFactory(nameTag));
        return pool;
    }

    public static ExecutorService createCustomPool(String nameTag, int core, int max) {
        ExecutorService pool = new ThreadPoolExecutor(core, max, 30L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(2048),
                new NamedThreadFactory(nameTag));
        return pool;
    }

    public static ExecutorService createSinglePool(String nameTag) {
        ExecutorService pool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), new NamedThreadFactory(nameTag));
        return pool;
    }

}
