package com.andyadc.zookeeper.study.lock.jike;

import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

/**
 * @author andaicheng
 * @version 2016/10/23
 */
public class TestDistributedLock {

    private static final String ZK_SERVER_ADDRESS = "139.196.192.166:2191,139.196.192.166:2192,139.196.192.166:2193";

    public static void main(String[] args) {
        final ZkClientExt zkClientExt1 = new ZkClientExt(ZK_SERVER_ADDRESS, 5000, 5000, new BytesPushThroughSerializer());
        final DistributedLock mutex1 = new SimpleDistributedLockMutex(zkClientExt1, "/mutex");

        final ZkClientExt zkClientExt2 = new ZkClientExt(ZK_SERVER_ADDRESS, 5000, 5000, new BytesPushThroughSerializer());
        final DistributedLock mutex2 = new SimpleDistributedLockMutex(zkClientExt2, "/mutex");

        try {
            mutex1.acquire();
            System.out.println("Client1 locked");
            Thread client2Thd = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mutex2.acquire();
                        System.out.println("Client2 locked");
                        mutex2.release();
                        System.out.println("Client2 released lock");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            client2Thd.start();
            Thread.sleep(5000);
            mutex1.release();
            System.out.println("Client1 released lock");

            client2Thd.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
