package com.sjw.fastnetty.common;

import com.sjw.fastnetty.protocol.CmdPackage;

/**
 * @author shijiawei
 * @version RequestBefore.java, v 0.1
 * @date 2019/2/13
 * 请求 aop
 */
public interface RequestAfter {
    /**
     * aop 执行请求后置方法
     */
    void doAfter(CmdPackage request, CmdPackage response);
}
