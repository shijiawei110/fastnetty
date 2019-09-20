package com.sjw.fastnetty.protocol.serialize;

/**
 * @author shijiawei
 * @version SerializeMethodCode.java, v 0.1
 * @date 2018/10/13
 * 序列化方法代号(byte)
 */
public enum SerializeMethodCode {

    PROTO_STUFF((byte) 1),

    FAST_JSON((byte) 2);

    private final byte code;

    SerializeMethodCode(byte code) {
        this.code = code;
    }


    public byte getCode() {
        return code;
    }

}
