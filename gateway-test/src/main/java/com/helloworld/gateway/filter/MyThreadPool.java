package com.helloworld.gateway.filter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * 线程池
 * 
 * @author zyj
 */
@Configuration
public class MyThreadPool {

    public static ThreadPoolExecutor poolExecutor;

    @PostConstruct
    public void initPool() {
        MyThreadPool.poolExecutor = new ThreadPoolExecutor(20, 50, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new DefaultThreadFactory("gateway-work-thread"));
    }

    /**
     * 异步处理
     * 
     * @param supplier
     * @return
     */
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier, poolExecutor);
    }

    /**
     * 异步处理问题
     * 
     * @param runnable
     * @return
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, poolExecutor);
    }
}
