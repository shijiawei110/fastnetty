package com.sjw.fastnetty.common;

/**
 * @author shijiawei
 * @version ReqCmdError.java, v 0.1
 * @date 2019/2/13
 * 处理请求 网络层 错误码枚举
 */
public enum ReqCmdError {

    SYSTEM_ERROR("系统错误", 9999),
    SYSTEM_BUSY("系统处理繁忙", 9998),
    NO_PROCESSOR("无对应请求应答处理器", 9901);

    private Integer code;
    private String msg;

    ReqCmdError(String msg, Integer code) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
