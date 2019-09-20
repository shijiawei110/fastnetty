package com.sjw.fastnetty.common;

import lombok.Data;

import java.util.concurrent.ExecutorService;

/**
 * @author shijiawei
 * @version ReqCmdProcessorHolder.java, v 0.1
 * @date 2019/2/2
 */
@Data
public class ReqCmdProcessorHolder {

    /**
     * 处理器
     **/
    private ReqCmdProcessor reqCmdProcessor;
    /**
     * 处理器自带线程池
     **/
    private ExecutorService executorService;

    public ReqCmdProcessorHolder(ReqCmdProcessor reqCmdProcessor, ExecutorService executorService) {
        this.reqCmdProcessor = reqCmdProcessor;
        this.executorService = executorService;
    }

}
