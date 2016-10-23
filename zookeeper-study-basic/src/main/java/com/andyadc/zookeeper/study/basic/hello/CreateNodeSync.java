package com.andyadc.zookeeper.study.basic.hello;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author andaicheng
 * @version 2016/10/23
 */
public class CreateNodeSync implements Watcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateNodeSync.class);

    private static final String IP = "139.196.192.166";
    private static ZooKeeper zooKeeper;

    static {
        try {
            zooKeeper = new ZooKeeper(IP + ":2191", 5000, new CreateNodeSync());
            LOGGER.info("{}", zooKeeper.getState());
        } catch (IOException e) {
            LOGGER.error("connect error", e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(Integer.MAX_VALUE);
    }

    private void doSomething() {
        String result;
        try {
            result = zooKeeper.create("/node2", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            LOGGER.info("create result: {}", result);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        LOGGER.info("收到事件: {}", event);
        if (event.getState() == Event.KeeperState.SyncConnected) {
            doSomething();
        }
    }
}
