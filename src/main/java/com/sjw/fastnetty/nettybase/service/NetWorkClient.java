package com.sjw.fastnetty.nettybase.service;

import com.sjw.fastnetty.common.ReqCmdProcessorHolder;
import com.sjw.fastnetty.common.RequestAfter;
import com.sjw.fastnetty.common.RequestBefore;
import com.sjw.fastnetty.protocol.CmdPackage;

/**
 * @author shijiawei
 * @version NetWorkClient.java, v 0.1
 * @date 2019/1/22
 * netty客户端接口
 */
public interface NetWorkClient extends NetWorkComService {
    /**
     * 同步指令
     */
    CmdPackage cmdSync(final String address, final CmdPackage cmd) throws InterruptedException;

    /**
     * 单向指令
     */
    void cmdOneWay(final String address, final CmdPackage cmd) throws InterruptedException;

    /**
     * 注册处理器
     */
    void registerCmdProcessor(Integer code, ReqCmdProcessorHolder holder);

    /**
     * 注册请求前置方法
     */
    void registerRequestBefore(RequestBefore requestBefore);

    /**
     * 注册请求后置方法
     */
    void registerRequestAfter(RequestAfter requestAfter);
}
