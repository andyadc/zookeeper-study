package com.andyadc.zookeeper.study.basic.hello;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author andaicheng
 * @version 2016/10/23
 */
public class CreateNodeAsyncAuth implements Watcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateNodeAsyncAuth.class);

    private static final String IP = "139.196.192.166";
    private static ZooKeeper zooKeeper;

    static {
        try {
            zooKeeper = new ZooKeeper(IP + ":2191", 5000, new CreateNodeAsyncAuth());
            LOGGER.info("{}", zooKeeper.getState());
        } catch (IOException e) {
            LOGGER.error("connect error", e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(Integer.MAX_VALUE);
    }

    private void doSomething() throws NoSuchAlgorithmException {
        //基于ip
        ACL acl = new ACL(ZooDefs.Perms.READ, new Id("ip", "114.86.164.36"));
        //基于用户名密码
        String digest = DigestAuthenticationProvider.generateDigest("adc:123456");
        ACL aclDigest = new ACL(ZooDefs.Perms.READ | ZooDefs.Perms.WRITE, new Id("digest", digest));

        List<ACL> acls = new ArrayList<>(Arrays.asList(acl, aclDigest));

        zooKeeper.create("/node4", "".getBytes(), acls, CreateMode.PERSISTENT, new IStringCallback(), "create node");
    }

    @Override
    public void process(WatchedEvent event) {
        LOGGER.info("收到事件: {}", event);
        if (event.getState() == Event.KeeperState.SyncConnected) {
            try {
                doSomething();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    static class IStringCallback implements AsyncCallback.StringCallback {
        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            LOGGER.info("code: {}", rc);
            LOGGER.info("path: {}", path);
            LOGGER.info("name: {}", name);
            LOGGER.info("context: {}", ctx);
        }
    }
}
