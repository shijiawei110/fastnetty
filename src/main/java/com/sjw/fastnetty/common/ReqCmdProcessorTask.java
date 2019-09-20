package com.sjw.fastnetty.common;

import com.sjw.fastnetty.protocol.CmdPackage;
import com.sjw.fastnetty.utils.ChannelHelper;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * @author shijiawei
 * @version ReqCmdProcessorTask.java, v 0.1
 * @date 2019/2/13
 */
@Slf4j
public class ReqCmdProcessorTask implements Runnable {

    private CmdPackage request;

    private Channel channel;

    private RequestBefore requestBefore;

    private RequestAfter requestAfter;

    private ReqCmdProcessor processor;

    public ReqCmdProcessorTask(CmdPackage request, Channel channel, ReqCmdProcessor processor,
                               RequestBefore requestBefore, RequestAfter requestAfter) {
        this.request = request;
        this.channel = channel;
        this.processor = processor;
        this.requestBefore = requestBefore;
        this.requestAfter = requestAfter;
    }

    @Override
    public void run() {
        try {
            // -> 3
            if (null != requestBefore) {
                requestBefore.doBfore(request);
            }
            // -> 4
            CmdPackage response = processor.executeRequest(channel, request);
            // -> 5
            if (null != requestAfter) {
                requestAfter.doAfter(request, response);
            }
            // -> 6
            if (!request.isOneWay()) {
                if (null != response) {
                    channel.writeAndFlush(response);
                }
            }
        } catch (Exception e) {
            //全局异常抓取
            ReqCmdError reqCmdError = ReqCmdError.SYSTEM_ERROR;
            CmdPackage response = CmdPackage.errorRes(request.getSn(), reqCmdError.getCode(), reqCmdError.getMsg());
            channel.writeAndFlush(response);
            log.error("fastnetty server processor request error -> addr={} ,request ={},stack={}",
                    ChannelHelper.getRemoteAddr(channel), request.toString(), ExceptionUtils.getStackTrace(e));
        }
    }
}
