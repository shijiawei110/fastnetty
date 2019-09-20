package com.sjw.fastnetty.protocol;

import com.sjw.fastnetty.common.ReqCmdError;
import lombok.Data;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shijiawei
 * @version CmdPackage.java, v 0.1
 * @date 2019/1/23
 * 通讯请求包
 */
@Data
public class CmdPackage {

    private static AtomicLong SN = new AtomicLong(0);
    /**
     * 唯一请求序号
     **/
    private long sn;
    /**
     * 指令类型
     **/
    private CmdType cmdType;
    /**
     * 具体命令请求类型(指令为request的时候才有)
     **/
    private Integer cmdCode;
    /**
     * 应答码
     */
    private ResponseCodeType responseCodeType;
    /**
     * 应答错误码(指令为res的时候才有)
     **/
    private Integer errorCode;
    /**
     * 应答错误信息(指令为res的时候才有)
     **/
    private String errorMsg;

    /**
     * 请求报文
     */
    private Object request;

    /**
     * 回复报文
     *
     * @return
     */
    private Object response;


    public boolean isOneWay() {
        if (cmdType == CmdType.ONE_WAY_REQ) {
            return true;
        }
        return false;
    }


    public static CmdPackage successRes(long sn) {
        return createRes(sn, ResponseCodeType.SUCCESS, null, null, null);
    }

    public static CmdPackage successRes(long sn, Object response) {
        return createRes(sn, ResponseCodeType.SUCCESS, null, null, response);
    }

    public static CmdPackage errorRes(long sn, Integer errorCode, String errorMsg) {
        return createRes(sn, ResponseCodeType.ERROR, errorCode, errorMsg, null);
    }

    public static CmdPackage errorRes(long sn, ReqCmdError reqCmdError) {
        return createRes(sn, ResponseCodeType.ERROR, reqCmdError.getCode(), reqCmdError.getMsg(), null);
    }

    public static CmdPackage createRes(long sn, ResponseCodeType responseCodeType, Integer errorCode, String errorMsg, Object response) {
        return create(sn, responseCodeType, CmdType.RES, null, null, errorCode, errorMsg, response);
    }

    public static CmdPackage createReq(Integer cmdCode, Object request) {
        return create(SN.getAndIncrement(), null, CmdType.REQ, cmdCode, request, null, null, null);
    }

    public static CmdPackage createOneWayReq(Integer cmdCode, Object request) {
        return create(-1, null, CmdType.ONE_WAY_REQ, cmdCode, request, null, null, null);
    }

    private static CmdPackage create(long sn, ResponseCodeType responseCodeType,
                                     CmdType cmdType, Integer cmdCode,
                                     Object request,
                                     Integer errorCode, String errorMsg,
                                     Object response) {
        CmdPackage cmdPackage = new CmdPackage();
        cmdPackage.setSn(sn);
        cmdPackage.setResponseCodeType(responseCodeType);
        cmdPackage.setCmdType(cmdType);
        cmdPackage.setCmdCode(cmdCode);
        cmdPackage.setRequest(request);
        cmdPackage.setResponse(response);
        cmdPackage.setErrorCode(errorCode);
        cmdPackage.setErrorMsg(errorMsg);
        return cmdPackage;
    }


    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
