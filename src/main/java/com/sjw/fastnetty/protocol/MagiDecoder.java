package com.sjw.fastnetty.protocol;

import com.sjw.fastnetty.protocol.serialize.SerializeFactory;
import com.sjw.fastnetty.protocol.serialize.SerializeMethod;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author shijiawei
 * @version MagiDecoder.java, v 0.1
 * @date 2019/1/23
 * 协议码 : 协议码(4byte) | 协议版本号(1byte) | 编程语言 (1byte) | 序列化算法(1byte) | 数据长度(int也就是4byte) | 数据包(n字节)
 * 注意 : decoder 因为带有自己的缓冲区 所以无法sharable
 */
@Slf4j
public class MagiDecoder extends ByteToMessageDecoder {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

        String linkAddress = ctx.channel().remoteAddress().toString();

        //检查协议码
        int protocolCode = in.readInt();
        if (protocolCode != ProtocolConstant.MAGI_PROTOCOL_CODE) {
            ctx.close();
            log.info("magi decoder take a error so close channel -> cause of protocol code differ , addr={}", linkAddress);
            return;
        }

        //检查协议版本 不通过直接关闭连接
        byte protocolVersion = in.readByte();
        if (protocolVersion != ProtocolConstant.MAGI_PROTOCOL_VERSION) {
            ctx.close();
            log.info("magi decoder take a error so close channel -> cause of protocol version differ , addr={}", linkAddress);
            return;
        }

        //读取编程语言 暂时没啥软用
        in.readByte();

        //读取序列化编码,选取相应的反序列化
        byte serializeMethodCode = in.readByte();
        SerializeMethod serializeMethod = SerializeFactory.getSerializeMethod(serializeMethodCode);
        if (null == serializeMethod) {
            ctx.close();
            log.info("magi decoder take a error so close channel -> cause of serialize code is illegal , addr={}", linkAddress);
            return;
        }

        //读取数据包长度
        int dataLength = in.readInt();
        // 我们读到的消息体长度为0，这是不应该出现的情况.
        if (dataLength < 0) {
            ctx.close();
            log.info("magi decoder take a error so close channel -> cause of data length < 0 , addr={}", linkAddress);
            return;
        }

        //检查实际长度和协议长度
        int realLength = in.readableBytes();
        if (dataLength != realLength) {
            ctx.close();
            log.info("magi decoder take a error so close channel -> cause of data length < 0 , addr={} ," +
                    "protocolLengrh={}, realLength={}", linkAddress, dataLength, realLength);
            return;
        }

        //读取数据包
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        CmdPackage cmdPackage = serializeMethod.deserialize(CmdPackage.class, data);
        out.add(cmdPackage);
    }

}
