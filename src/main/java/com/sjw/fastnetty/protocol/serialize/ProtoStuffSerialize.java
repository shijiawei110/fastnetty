package com.sjw.fastnetty.protocol.serialize;

/**
 * @author shijiawei
 * @version ProtoStuffSerialize.java, v 0.1
 * @date 2019/1/24
 */
public class ProtoStuffSerialize implements SerializeMethod {

    private volatile static ProtoStuffSerialize instance;

    public static ProtoStuffSerialize getInstance() {
        if (instance == null) {
            synchronized (ProtoStuffSerialize.class) {
                if (instance == null) {
                    instance = new ProtoStuffSerialize();
                }
            }
        }
        return instance;
    }

    @Override
    public byte getSerializerMethodCode() {
        return SerializeMethodCode.PROTO_STUFF.getCode();
    }

    @Override
    public byte[] serialize(Object object) {
        return ProtoStuffUtil.serializer(object);
    }


    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return ProtoStuffUtil.deserializer(bytes, clazz);
    }

}
