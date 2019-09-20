package com.sjw.fastnetty.utils;

/**
 * @author shijiawei
 * @version SystemUtil.java, v 0.1
 * @date 2019/1/22
 * 系统相关方法
 */
public class SystemUtil {
    public static final String OS_NAME = System.getProperty("os.name");

    private static boolean isLinux = false;
    private static boolean isWindows = false;

    static {
        if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
            isLinux = true;
        }

        if (OS_NAME != null && OS_NAME.toLowerCase().contains("windows")) {
            isWindows = true;
        }
    }

    public static boolean isLinux(){
        return isLinux;
    }

    public static boolean isWindows(){
        return isWindows;
    }

}
