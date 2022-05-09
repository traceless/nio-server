package com.helloworld.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

import com.helloworld.springboot.util.HttpClientUtil;
import com.helloworld.springboot.util.MyThreadPool;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * @author doctor
 */
@RestController()
@RequestMapping(value = "/")
@Slf4j
public class CompletableFutureCase {

    /**
     * 请求服务
     * @param time
     * @param req
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @RequestMapping("/comFutrue/{time}")
    public String comFutrue(@PathVariable("time") int time, HttpServletRequest req)
            throws InterruptedException, ExecutionException {

        log.info("------comFutrue------{}", System.currentTimeMillis());
        // 接口响应时间取决time参数，创建异步任务并且有返回结果
        String url = "http://127.0.0.1:7080/test/" + time;
        CompletableFuture<String> httpResFuture = CompletableFuture.supplyAsync(() -> {
            return HttpClientUtil.getByUrl(url);
        });
        // get方法在这里是阻塞等待的
        String coffee = httpResFuture.get();
        // do other business
        String data = doOtherBusiness(null);
        return coffee + data;
    }

    /**
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public void testCallable() throws InterruptedException, ExecutionException {
        FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(2000);
                return "ok";
            }
        });
        MyThreadPool.poolExecutor.execute(futureTask);
        // do subBusiness
        subBusiness();
        log.info("---testCallable:{}", futureTask.get());
    }

    /**
     * 异步任务
     * @param time
     * @param req
     * @throws IOException
     */
    @RequestMapping("/startAsync/{time}")
    public void startAsync(@PathVariable("time") int time, HttpServletRequest req) throws IOException {
        log.info("------startAsync------{}", System.currentTimeMillis());
        final AsyncContext ctx = req.getAsyncContext();
        ctx.start(() -> {
            // 异步处理任务
            String data = doOtherBusiness(null);
            log.info("doOtherBusiness:{}", data);
        });
    }

    /**
     * 模拟IO业务
     * 
     * @param req
     * @return
     */
    private String doOtherBusiness(String req) {
        String url = "http://127.0.0.1:7080/test/" + 20;
        return HttpClientUtil.getByUrl(url);
    }

    /**
     * 模拟IO业务
     * 
     * @param req
     * @return
     */
    private String subBusiness() {
        String url = "http://127.0.0.1:7080/test/" + 20;
        return HttpClientUtil.getByUrl(url);
    }
}
