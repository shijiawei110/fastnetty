package com.sjw.fastnetty.protocol;

import com.sjw.fastnetty.protocol.serialize.ProtoStuffSerialize;
import com.sjw.fastnetty.protocol.serialize.SerializeMethod;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shijiawei
 * @version MagiEncoder.java, v 0.1
 * @date 2019/1/23
 * 序列化协议
 * 协议码 : 协议码(4byte) | 协议版本号(1byte) | 编程语言 (1byte) | 序列化算法(1byte) | 数据长度(int也就是4byte) | 数据包(n字节)
 */
@Slf4j
@ChannelHandler.Sharable
public class MagiEncoder extends MessageToByteEncoder {

    private volatile static MagiEncoder instance;

    public static MagiEncoder getInstance() {
        if (instance == null) {
            synchronized (MagiEncoder.class) {
                if (instance == null) {
                    instance = new MagiEncoder();
                }
            }
        }
        return instance;
    }

    /**
     * 默认暂时使用 protostuff
     */
    private SerializeMethod serializeMethod = ProtoStuffSerialize.getInstance();

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) {
        byte[] data = serializeMethod.serialize(in);
        //写入协议码
        out.writeInt(ProtocolConstant.MAGI_PROTOCOL_CODE);
        //写入版本号
        out.writeByte(ProtocolConstant.MAGI_PROTOCOL_VERSION);
        //写入编程语言 - java
        out.writeByte(ProtocolConstant.MAGI_CODE_LANGUAGE);
        //写入序列化编码
        out.writeByte(serializeMethod.getSerializerMethodCode());
        //写入数据长度
        out.writeInt(data.length);
        //写入正文数据包
        out.writeBytes(data);
    }

}
