package com.sjw.fastnetty.exception;

import java.text.MessageFormat;

/**
 * @author shijw
 * @version MagiException.java, v 0.1 2018-10-07 20:05 shijw
 */
public class MagiException extends RuntimeException {
    /**
     * 参数异常
     */
    public static final MagiException PARAMS_ERROR = new MagiException(1001, "参数错误");

    public static final MagiException SEND_CMD_OUT_TIME = new MagiException(2001, "发送请求超时");
    public static final MagiException SEND_CMD_FAIL = new MagiException(2002, "发送请求失败");

    public static final MagiException CLIENT_GET_CHANNEL_ERROR = new MagiException(3001, "客户端获取连接异常");
    public static final MagiException CLIENT_REQ_OUT_TIME = new MagiException(3002, "客户端连接服务超时");
    public static final MagiException CLIENT_GET_CHANNEL_TABLE_LOCK_OUT_TIME = new MagiException(3003, "客户端获取连接表aqs锁超时");
    public static final MagiException CLIENT_ADD_SERVER_NODE_PARAMS_ERROR= new MagiException(3004, "客户端添加服务缓存信息失败(参数不正确)");
    public static final MagiException CLIENT_INIT_REGISTER_FAIL= new MagiException(3005, "客户端初始化注册到magi服务失败");


    public static final MagiException CLIENT_SYSYTEM_ENDING = new MagiException(9998, "magi客户端系统正在关闭,无法发出请求");
    public static final MagiException SERVER_SYSYTEM_ENDING = new MagiException(9999, "magi服务端系统正在关闭,无法发出请求");


    /**
     * 异常信息
     */
    protected String msg;

    /**
     * 具体异常码
     */
    protected int code;

    /**
     * 异常构造器
     *
     * @param code      代码
     * @param msgFormat 消息模板,内部会用MessageFormat拼接，模板类似：userid={0},message={1},date{2}
     * @param args      具体参数的值
     */
    private MagiException(int code, String msgFormat, Object... args) {
        super(MessageFormat.format(msgFormat, args));
        this.code = code;
        this.msg = MessageFormat.format(msgFormat, args);
    }

    /**
     * 默认构造器
     */
    private MagiException() {
        super();
    }

    /**
     * 异常构造器
     *
     * @param message
     * @param cause
     */
    private MagiException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 异常构造器
     *
     * @param cause
     */
    private MagiException(Throwable cause) {
        super(cause);
    }

    /**
     * 异常构造器
     *
     * @param message
     */
    private MagiException(String message) {
        super(message);
    }

    /**
     * 实例化异常
     *
     * @return 异常类
     */
    public MagiException newInstance(String msgFormat, Object... args) {
        return new MagiException(this.code, msgFormat, args);
    }


}
