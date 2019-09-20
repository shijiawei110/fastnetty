package com.sjw.fastnetty.common;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author shijiawei
 * @version TcpPackageHander.java, v 0.1
 * @date 2018/10/18
 * 自定义tcp粘拆包处理器
 * 偏移7位 长度区域4位
 * 注意：无法写成单例,因为携带自己的channel状态作为数据缓冲来粘包拆包
 */

public class TcpPackageHander extends LengthFieldBasedFrameDecoder {

    private static final int LENGTH_FIELD_OFFSET = 7;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int MAX_LENGTH = Integer.MAX_VALUE;

    public TcpPackageHander() {
        super(MAX_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH,0,0);
    }

}
