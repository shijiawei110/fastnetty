package com.sjw.fastnetty.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author shijiawei
 * @version SyncPollingUtil.java -> v 1.0
 * @date 2019/3/29
 * 轮询工具
 */
@Slf4j
public class SyncPollingUtil {
    private static final long MAX_WAIT_MILLS = 10000L;
    private static final int MAX_WAIT_COUNT = 100;

    /**
     * @param allMills  总共轮询多少毫秒
     * @param count     每次轮询等待时间
     * @param condition 跳出条件
     */
    public static void polling(long allMills, int count, SyncPollingCondition condition) {
        allMills = allMills > MAX_WAIT_MILLS ? MAX_WAIT_MILLS : allMills;
        count = count > MAX_WAIT_COUNT ? MAX_WAIT_COUNT : count;
        int permills = (int) (allMills / count);

        try {
            for (int i = 0; i < count; i++) {
                if (condition.isBreak()) {
                    return;
                }
                Thread.sleep(permills);
            }
        } catch (InterruptedException e) {
            log.error("SyncPollingUtil polling error , just return", e);
        }
    }
}
