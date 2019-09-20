package com.sjw.fastnetty.protocol.serialize;

/**
 * @author shijiawei
 * @version SerializeMethod.java, v 0.1
 * @date 2018/10/13
 */
public interface SerializeMethod {
    /**
     * 序列化算法代号
     */
    byte getSerializerMethodCode();

    /**
     *  序列化
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);

}
