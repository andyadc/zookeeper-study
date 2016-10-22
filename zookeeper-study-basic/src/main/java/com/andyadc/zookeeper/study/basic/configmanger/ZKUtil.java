package com.andyadc.zookeeper.study.basic.configmanger;

import org.I0Itec.zkclient.ZkClient;

/**
 * @author andaicheng
 * @version 2016/10/22
 */
public class ZKUtil {

    private static final String IP = "139.196.192.166";
    public static final String CONFIG_NODE_NAME = "/config/ftp";

    public static ZkClient getZkclient() {
        return new ZkClient(IP + ":2191," + IP + ":2192," + IP + ":2193");
    }
}
