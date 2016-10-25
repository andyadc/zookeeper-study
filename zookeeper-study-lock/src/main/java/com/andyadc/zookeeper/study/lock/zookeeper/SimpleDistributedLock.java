package com.andyadc.zookeeper.study.lock.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 利用zookeeper的EPHEMERAL_SEQUENTIAL类型节点及watcher机制，来简单实现分布式锁<br>
 * 主要思想：
 * 1、开启10个线程，在disLocks节点下各自创建名为sub的EPHEMERAL_SEQUENTIAL节点；
 * 2、获取disLocks节点下所有子节点，排序，如果自己的节点编号最小，则获取锁；
 * 3、否则watch排在自己前面的节点，监听到其删除后，进入第2步（重新检测排序是防止监听的节点发生连接失效，导致的节点删除情况）；
 * 4、删除自身sub节点，释放连接；
 * <ur>http://blog.csdn.net/desilting/article/details/41280869</ur>
 *
 * @author andaicheng
 * @version 2016/10/21
 */
public class SimpleDistributedLock implements Watcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDistributedLock.class);

    private ZooKeeper zk;
    private int threadId;
    private String selfPath;
    private String waitPath;
    private String LOG_PREFIX_OF_THREAD;

    private static final int SESSION_TIMEOUT = 10000;
    private static final String GROUP_PATH = "/disLocks";
    private static final String SUB_PATH = "/disLocks/sub";
    private static final String CONNECTION_STRING = "139.196.192.166:2191,139.196.192.166:2192,139.196.192.166:2193";
    private static final int THREAD_NUM = 10;

    //确保连接zk成功
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);
    //确保所有线程运行结束
    private static final CountDownLatch threadSemaphore = new CountDownLatch(THREAD_NUM);

    public SimpleDistributedLock(int id) {
        this.threadId = id;
        LOG_PREFIX_OF_THREAD = "【第" + threadId + "个线程】";
    }

    public static void main(String[] args) {
        for (int i = 0; i < THREAD_NUM; i++) {
            final int threadId = i + 1;
            new Thread() {
                @Override
                public void run() {
                    try {
                        SimpleDistributedLock lock = new SimpleDistributedLock(threadId);
                        lock.createConnection(CONNECTION_STRING, SESSION_TIMEOUT);
                        synchronized (threadSemaphore) {
                            lock.createPath(GROUP_PATH, "该节点由线程" + threadId + "创建", true);
                        }
                        lock.getLock();
                    } catch (Exception e) {
                        LOGGER.error("【第" + threadId + "个线程】 抛出的异常：");
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        try {
            threadSemaphore.await();
            LOGGER.info("所有线程运行结束!");
        } catch (Exception e) {
            LOGGER.error("线程运行异常");
        }
    }

    private void getLock() throws KeeperException, InterruptedException {
        selfPath = zk.create(SUB_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        LOGGER.info(LOG_PREFIX_OF_THREAD + "创建锁路径:" + selfPath);
        if (checkMinPath()) {
            getLockSuccess();
        }
    }

    public boolean createPath(String path, String data, boolean needWatch) throws KeeperException, InterruptedException {
        if (zk.exists(path, needWatch) == null) {
            LOGGER.info(LOG_PREFIX_OF_THREAD + "节点创建成功, Path: "
                    + this.zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
                    + ", content: " + data);
        }
        return true;
    }

    public void createConnection(String connectString, int sessionTimeout) throws IOException, InterruptedException {
        zk = new ZooKeeper(CONNECTION_STRING, sessionTimeout, this);
        connectedSemaphore.await();
    }

    public void getLockSuccess() throws KeeperException, InterruptedException {
        if (zk.exists(this.selfPath, false) == null) {
            LOGGER.error(LOG_PREFIX_OF_THREAD + "本节点已不在了...");
            return;
        }
        LOGGER.info(LOG_PREFIX_OF_THREAD + "获取锁成功，赶紧干活！");
        Thread.sleep(2000);
        LOGGER.info(LOG_PREFIX_OF_THREAD + "删除本节点: " + selfPath);
        zk.delete(this.selfPath, -1);
        releaseConnection();
        threadSemaphore.countDown();
    }

    /**
     * 关闭ZK连接
     */
    public void releaseConnection() {
        if (this.zk != null) {
            try {
                zk.close();
            } catch (InterruptedException e) {
            }
        }
        LOGGER.info(LOG_PREFIX_OF_THREAD + "释放连接");
    }

    /**
     * 检查自己是不是最小的节点
     *
     * @return
     */
    public boolean checkMinPath() throws KeeperException, InterruptedException {
        List<String> subNodes = zk.getChildren(GROUP_PATH, false);
        Collections.sort(subNodes);
        int index = subNodes.indexOf(selfPath.substring(GROUP_PATH.length() + 1));
        switch (index) {
            case -1: {
                LOGGER.error(LOG_PREFIX_OF_THREAD + "本节点已不在了..." + selfPath);
                return false;
            }
            case 0: {
                LOGGER.info(LOG_PREFIX_OF_THREAD + "子节点中，我果然是老大" + selfPath);
                return true;
            }
            default: {
                this.waitPath = GROUP_PATH + "/" + subNodes.get(index - 1);
                LOGGER.info(LOG_PREFIX_OF_THREAD + "获取子节点中，排在我前面的" + waitPath);
                try {
                    zk.getData(waitPath, true, new Stat());
                    return false;
                } catch (KeeperException e) {
                    if (zk.exists(waitPath, false) == null) {
                        LOGGER.info(LOG_PREFIX_OF_THREAD + "子节点中，排在我前面的" + waitPath + "已失踪，幸福来得太突然?");
                        return checkMinPath();
                    } else {
                        throw e;
                    }
                }
            }

        }

    }

    @Override
    public void process(WatchedEvent event) {
        if (event == null) {
            return;
        }
        Event.KeeperState keeperState = event.getState();
        Event.EventType eventType = event.getType();
        if (Event.KeeperState.SyncConnected == keeperState) {
            if (Event.EventType.None == eventType) {
                LOGGER.info(LOG_PREFIX_OF_THREAD + "成功连接上ZK服务器");
                connectedSemaphore.countDown();
            } else if (event.getType() == Event.EventType.NodeDeleted && event.getPath().equals(waitPath)) {
                LOGGER.info(LOG_PREFIX_OF_THREAD + "收到情报，排我前面的家伙已挂，我是不是可以出山了？");
                try {
                    if (checkMinPath()) {
                        getLockSuccess();
                    }
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (Event.KeeperState.Disconnected == keeperState) {
            LOGGER.info(LOG_PREFIX_OF_THREAD + "与ZK服务器断开连接");
        } else if (Event.KeeperState.AuthFailed == keeperState) {
            LOGGER.info(LOG_PREFIX_OF_THREAD + "权限检查失败");
        } else if (Event.KeeperState.Expired == keeperState) {
            LOGGER.info(LOG_PREFIX_OF_THREAD + "会话失效");
        }
    }
}
