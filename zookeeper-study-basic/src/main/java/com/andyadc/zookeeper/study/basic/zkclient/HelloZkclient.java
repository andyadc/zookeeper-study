package com.andyadc.zookeeper.study.basic.zkclient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.util.Date;

/**
 * @author andaicheng
 * @version 2016/10/21
 */
public class HelloZkclient {

    private static final String IP = "139.196.192.166";

    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient(IP + ":2191," + IP + ":2192," + IP + ":2193", 15000, 5000, new SerializableSerializer());
        String node = "/zkc1";
        if (!zkClient.exists(node)) {
            zkClient.createPersistent(node, new Date());
        }
        Object ret = zkClient.readData(node);
        System.out.println(ret);
    }
}
