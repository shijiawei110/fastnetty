package com.sjw.fastnetty.nettybase.service;

import com.sjw.fastnetty.common.ReqCmdProcessorHolder;
import com.sjw.fastnetty.common.RequestAfter;
import com.sjw.fastnetty.common.RequestBefore;
import com.sjw.fastnetty.protocol.CmdPackage;
import io.netty.channel.Channel;

/**
 * @author shijiawei
 * @version NetWorkServer.java, v 0.1
 * @date 2019/1/22
 * netty服务端接口
 */
public interface NetWorkServer extends NetWorkComService {

    /**
     * 同步指令报文
     */
    CmdPackage cmdSync(final Channel channel, final CmdPackage cmd) throws InterruptedException;

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
