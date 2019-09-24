package com.sjw.fastnetty.callback;

import com.sjw.fastnetty.common.CmdFuture;

/**
 * @author shijiawei
 * @version AsyncCallBack.java -> v 1.0
 * @date 2019/9/24
 */
public interface AsyncCallBack {
    void execute(final CmdFuture cmdFuture);
}
