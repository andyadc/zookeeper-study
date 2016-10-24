package com.andyadc.zookeeper.study.lock.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

/**
 * @author andaicheng
 * @version 2016/10/24
 */
public class TestInterProcessMutex {

    private static final String SERVER = "139.196.192.166:2191";
    private static final String path1 = "/mutex";

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client1 = CuratorFrameworkFactory.newClient(SERVER, 5000, 5000, new ExponentialBackoffRetry(1000, 1));
        client1.start();
        InterProcessMutex mutex1 = new InterProcessMutex(client1, path1);

        CuratorFramework client2 = CuratorFrameworkFactory.newClient(SERVER, 5000, 5000, new ExponentialBackoffRetry(1000, 1));
        client2.start();
        InterProcessMutex mutex2 = new InterProcessMutex(client2, path1);

        try {
            mutex1.acquire();
            System.out.println("mutex1 get lock!");

            Thread thd2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mutex2.acquire();
                        System.out.println("mutex2 get lock!");
                        mutex2.release();
                        System.out.println("mutex2 release lock!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thd2.start();
            Thread.sleep(5000);
            mutex1.release();
            System.out.println("mutex1 release lock!");

            thd2.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client1);
            CloseableUtils.closeQuietly(client2);
        }
    }
}
