package com.helloworld.webflux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class PostRepository {

    /** 同步式客户端 */
    @Autowired
    RedisTemplate<String, String> template;

    /** 响应式客户端 */
    @Autowired
    ReactiveRedisOperations<String, String> reactiveRedisTemplate;

    String findById(String key) {
        return template.opsForValue().get(key);
    }

    Mono<String> monoFindById(String key) {
        return reactiveRedisTemplate.opsForValue().get(key);
    }

    void save(String key, String value) {
        template.opsForValue().set(key, value);
    }

}

