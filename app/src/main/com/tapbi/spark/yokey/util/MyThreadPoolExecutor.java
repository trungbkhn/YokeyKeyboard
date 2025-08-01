package com.tapbi.spark.yokey.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class MyThreadPoolExecutor {
    int corePoolSize = 2;
    int maximumPoolSize = 2;
    long keepAliveTime = 0L;
    TimeUnit unit = TimeUnit.SECONDS;
    LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    ThreadPoolExecutor executor;

    @Inject
    public MyThreadPoolExecutor() {
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public ThreadPoolExecutor executor() {
        return executor;
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }



}
