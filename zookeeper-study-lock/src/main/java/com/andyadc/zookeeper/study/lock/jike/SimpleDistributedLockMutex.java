package com.andyadc.zookeeper.study.lock.jike;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author andaicheng
 * @version 2016/10/23
 */
public class SimpleDistributedLockMutex extends BaseDistributedLock implements
        DistributedLock {

    //锁名称前缀
    private static final String LOCK_NAME = "lock-";

    private final String basePath;
    private String ourLockPath;

    public SimpleDistributedLockMutex(ZkClientExt client, String basePath) {
        super(client, basePath, LOCK_NAME);
        this.basePath = basePath;
    }

    private boolean internalLock(long time, TimeUnit unit) throws Exception {
        ourLockPath = attemptLock(time, unit);
        return ourLockPath != null;
    }

    public void acquire() throws Exception {
        if (!internalLock(-1, null)) {
            throw new IOException("连接丢失!在路径:'" + basePath + "'下不能获取锁!");
        }
    }

    public boolean acquire(long time, TimeUnit unit) throws Exception {
        return internalLock(time, unit);
    }

    public void release() throws Exception {
        releaseLock(ourLockPath);
    }
}