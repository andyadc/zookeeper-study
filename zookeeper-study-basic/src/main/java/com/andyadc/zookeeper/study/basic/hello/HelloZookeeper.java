package com.andyadc.zookeeper.study.basic.hello;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author andaicheng
 * @version 2016/10/21
 */
public class HelloZookeeper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloZookeeper.class);

    private static final String IP = "139.196.192.166";

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        ZooKeeper zk = new ZooKeeper(IP + ":2191", 10000, new DemoWatcher());
        String node = "/zk";
        Stat stat = zk.exists(node, false);
        if (stat == null) {
            String ceateResult = zk.create(node, "hello".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            LOGGER.info("result: {}", ceateResult);
        }

        byte[] bytes = zk.getData(node, false, stat);
        LOGGER.info("bytes: {}", new String(bytes));
        zk.close();
    }

}

/**
 *  监听器
 */
class DemoWatcher implements Watcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoWatcher.class);

    @Override
    public void process(WatchedEvent event) {
        LOGGER.info("----------->");
        LOGGER.info("path:" + event.getPath());
        LOGGER.info("type:" + event.getType());
        LOGGER.info("stat:" + event.getState());
        LOGGER.info("<-----------");
    }
}