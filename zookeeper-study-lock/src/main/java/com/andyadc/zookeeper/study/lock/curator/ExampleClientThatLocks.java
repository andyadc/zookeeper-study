package com.andyadc.zookeeper.study.lock.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author andaicheng
 */
public class ExampleClientThatLocks {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleClientThatLocks.class);

    private final InterProcessLock lock;
    private final FakeLimitedResource resource;
    private final String clientName;

    public ExampleClientThatLocks(CuratorFramework framework, String lockPath, FakeLimitedResource resource, String clientName) {
        this.resource = resource;
        this.clientName = clientName;
        lock = new InterProcessMutex(framework, lockPath);
    }

    public void doWork(long time, TimeUnit unit) throws Exception {
        if (!lock.acquire(time, unit)) {
            LOGGER.error(clientName + " could not acquire the lock");
            throw new IllegalStateException(clientName + " could not acquire the lock");
        }
        try {
            LOGGER.info(clientName + " has the lock");
            resource.use(); //access resource exclusively
        } finally {
            LOGGER.info(clientName + " releasing the lock");
            lock.release(); // always release the lock in a finally block
        }
    }
}
