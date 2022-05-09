package com.helloworld.springboot.controller;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

import com.helloworld.springboot.util.HttpClientUtil;
import com.helloworld.springboot.util.VertxHttpClientUtil;

import org.springframework.web.bind.annotation.GetMapping;
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
public class TestAsync {

    /**
     * 测试接口
     * 
     * @param time
     * @return
     * @throws InterruptedException
     */
    @RequestMapping("/test/{time}")
    public String test(@PathVariable("time") int time, HttpServletRequest req) throws InterruptedException {
        // 要异步输出到console，不然影响性能测试，实际项目发布运行，不需要打开console查看的，问题不大
        log.info("---test--:{}", time);
        if (time > 0) {
            // 模拟IO业务执行时的等待
            Thread.sleep(time);
        }
        return "hello world";
    }

    @GetMapping("/getPrepayId")
    public String getPrepayId(String userId, String orderId) throws Exception {
        // TODO，收到APP请求后，需要请求微服务B，或者预支付订单号
        // 代码如何实现，要求接口满足达到1W+/S的并发
        String data = getPIdFromServerB(100);
        return data;
    }

    public String getPIdFromServerB(@PathVariable("time") int time) throws Exception {
        System.out.println("-----testAsync------" + System.currentTimeMillis());
        String url = "http://127.0.0.1:7080/test/" + time;
        String res = HttpClientUtil.get(url, null);
        return res;
    }

    /**
     * 测试异步处理请求
     * 
     * @param req
     * @throws Exception
     */
    @RequestMapping("/testAsync/{time}")
    public void testAsync(@PathVariable("time") int time, HttpServletRequest req) throws Exception {
        System.out.println("-----testAsync------" + System.currentTimeMillis());
        String url = "http://127.0.0.1:7080/test/" + time;
        final AsyncContext ctx = req.startAsync();
        VertxHttpClientUtil.getClient().post(url, (data) -> {
            try {
                ctx.getResponse().getWriter().print(data);
                ctx.complete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
