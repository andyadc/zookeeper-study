package com.andyadc.zookeeper.study.basic.zkclient;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.util.List;

/**
 * @author andaicheng
 * @version 2016/10/23
 */
public class SubscribeChildrenChange {

    private static final String IP = "139.196.192.166";

    public static void main(String[] args) throws InterruptedException {
        ZkClient client = new ZkClient(IP + ":2191", 10000, 10000, new SerializableSerializer());

        client.subscribeChildChanges("/zkc1", new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                System.out.println(parentPath);
                System.out.println(currentChilds);
            }
        });

        Thread.sleep(Integer.MAX_VALUE);
    }
}
