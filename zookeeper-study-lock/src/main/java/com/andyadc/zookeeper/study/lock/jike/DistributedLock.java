package com.andyadc.zookeeper.study.lock.jike;

import java.util.concurrent.TimeUnit;

/**
 * @author andaicheng
 * @version 2016/10/23
 */
public interface DistributedLock {

    /**
     * 获取锁, 如果没有就等待
     */
    void acquire() throws Exception;

    /**
     * 获取锁, 直到超时
     */
    boolean acquire(long time, TimeUnit unit) throws Exception;

    /**
     * 释放锁
     */
    void release() throws Exception;
}
