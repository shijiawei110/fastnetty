package com.sjw.fastnetty.protocol.serialize;

import com.alibaba.fastjson.JSON;

/**
 * @author shijiawei
 * @version JsonSerialize.java, v 0.1
 * @date 2018/10/13
 * fastJson 序列化 -》一个大坑 反序列化后 反射中 参数值如果是自定义pojo会反序列化为jsonobject,无法执行反射invoker,可以用其他序列化方法试试
 */
public class JsonSerialize implements SerializeMethod {

    private volatile static JsonSerialize instance;

    public static JsonSerialize getInstance() {
        if (instance == null) {
            synchronized (JsonSerialize.class) {
                if (instance == null) {
                    instance = new JsonSerialize();
                }
            }
        }
        return instance;
    }

    @Override
    public byte getSerializerMethodCode() {
        return SerializeMethodCode.FAST_JSON.getCode();
    }

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }


    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }


}
