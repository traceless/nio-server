package com.helloworld.springboot.controller;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

import com.helloworld.springboot.PostRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author doctor
 */
@RestController()
@RequestMapping(value = "/")
public class RedisAsync {
    
    @Autowired
    private PostRepository repository;
    
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
        // ctx.start(() ->{
        //     String name = Thread.currentThread().getName();
        //     System.out.println(" == = == " + name);
        // });
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
