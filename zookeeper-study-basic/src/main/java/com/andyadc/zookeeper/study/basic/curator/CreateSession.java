package com.andyadc.zookeeper.study.basic.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * @author andaicheng
 * @version 2016/10/23
 */
public class CreateSession {

    private static final String IP = "139.196.192.166";

    public static void main(String[] args) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework framework = CuratorFrameworkFactory.newClient(IP + ":2191", 5000, 5000, retryPolicy);
        framework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/curator/c1", "123".getBytes());

        framework.delete().deletingChildrenIfNeeded().forPath("/curator");
        framework.close();
    }
}
