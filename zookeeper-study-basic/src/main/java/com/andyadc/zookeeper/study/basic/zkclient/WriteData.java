package com.andyadc.zookeeper.study.basic.zkclient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.util.Date;

/**
 * @author andaicheng
 * @version 2016/10/23
 */
public class WriteData {

    private static final String IP = "139.196.192.166";

    public static void main(String[] args) {
        ZkClient client = new ZkClient(IP + ":2191", 10000, 10000, new SerializableSerializer());

        client.writeData("/zkc1", new Date());

    }
}
