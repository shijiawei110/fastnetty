package com.sjw.fastnetty.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

/**
 * @author shijiawei
 * @version ThreadNameUtil.java -> v 1.0
 * @date 2019/4/10
 */
public class ThreadNameUtil {

    private static final String DEFAULT_THREAD_NAME = "fastnetty-thread";

    public static String getName(String threadName) {
        Random random = new Random();
        int nameIndex1 = random.nextInt(100);
        int nameIndex2 = random.nextInt(100);
        int nameIndex3 = random.nextInt(100);
        int count = nameIndex1 + nameIndex2 + nameIndex3;
        String num = String.valueOf(count / 3);
        if (StringUtils.isBlank(threadName)) {
            return DEFAULT_THREAD_NAME + StringPool.DASH + num;
        } else {
            return threadName + StringPool.DASH + num;
        }
    }
}
