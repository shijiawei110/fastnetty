package com.sjw.fastnetty.protocol.serialize;

/**
 * @author shijiawei
 * @version SerializeFactory.java, v 0.1
 * @date 2019/1/24
 */
public class SerializeFactory {
    public static SerializeMethod getSerializeMethod(byte code){
        if(code == SerializeMethodCode.PROTO_STUFF.getCode()){
            return ProtoStuffSerialize.getInstance();
        }

        if(code == SerializeMethodCode.FAST_JSON.getCode()){
            return JsonSerialize.getInstance();
        }

        return null;
    }
}
