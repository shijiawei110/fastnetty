package com.sjw.fastnetty.client;

import com.sjw.fastnetty.common.CmdFuture;
import com.sjw.fastnetty.utils.SyncPollingCondition;

import java.util.concurrent.ConcurrentMap;

/**
 * @author shijiawei
 * @version SystemClosePolling.java -> v 1.0
 * @date 2019/3/29
 * 优雅关闭轮询器 ,直到 cmdContainer 清空 说明所有请求都被应答或者超时  强制关闭所有连接的 channel
 */
public class SystemClosePolling implements SyncPollingCondition {

    private ConcurrentMap<Long, CmdFuture> cmdContainer;

    public SystemClosePolling(ConcurrentMap<Long, CmdFuture> cmdContainer) {
        this.cmdContainer = cmdContainer;
    }

    @Override
    public boolean isBreak() {
        int noComleteCount = cmdContainer.size();
        if (noComleteCount <= 0) {
            return true;
        }
        return false;
    }
}
