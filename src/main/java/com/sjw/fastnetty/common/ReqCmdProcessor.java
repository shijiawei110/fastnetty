package com.sjw.fastnetty.common;

import com.sjw.fastnetty.protocol.CmdPackage;
import io.netty.channel.Channel;

/**
 * @author shijiawei
 * @version ReqCmdProcessor.java, v 0.1
 * @date 2019/2/1
 * 具体处理器接口
 */
public interface ReqCmdProcessor {
    CmdPackage executeRequest(Channel channel, CmdPackage requset);
}
