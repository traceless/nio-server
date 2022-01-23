package com.helloworld.springboot;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

import com.helloworld.springboot.util.HttpClientUtil;
import com.helloworld.springboot.util.VertxHttpClientUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * @author doctor
 * 
 */
@SpringBootApplication

public class SpringbootApplication {
    public static void main(String[] args) {

        SpringApplication.run(SpringbootApplication.class, args);
    }
}

@RestController()
@RequestMapping(value = "/")
@Slf4j
class WebController {

    @Autowired
    private PostRepository repository;

    /**
     * 测试接口
     * 
     * @param time
     * @return
     * @throws InterruptedException
     */
    @RequestMapping("/test/{time}")
    public String test(@PathVariable("time") int time) throws InterruptedException {
        // System.out.println("----test------" + System.currentTimeMillis());
        // log对性能测试影响还是挺大，不过改成异步之后，吞吐量就没问题了。主要是log是同步写入文件的。
        log.info("---test--:{}", time);
        if (time > 0) {
            Thread.sleep(time);
        }
        return "hello world";
    }

    @GetMapping("/getPrepayId")
    public String getPrepayId(String userId, String orderId) {
        // TODO，收到APP请求后，访问微信接口获取预支付订单号。
        // 代码如何实现，要求接口满足达到1W+/S的并发
        return "success";
    }

    /**
     * 传统的请求方式
     * 
     * @param time
     * @return
     * @throws Exception
     */
    @RequestMapping("/testBlock/{time}")
    public String testBlock(@PathVariable("time") int time) throws Exception {
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

    /**
     * 测试redis异步获取数据
     * 
     * @param req
     * @throws IOException
     */
    @RequestMapping("/redisAsync/{time}")
    public void redisAsync(@PathVariable("time") int time, HttpServletRequest req) throws IOException {
        System.out.println("-----redisAsync------" + System.currentTimeMillis());
        final AsyncContext ctx = req.startAsync();
        String key = "user:test:key";
        this.repository.monoFindById(key).subscribe(data -> {
            try {
                ctx.getResponse().getWriter().print(data);
                ctx.complete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
