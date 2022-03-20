package com.helloworld.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 
 * @author doctor
 */
@Component
@Slf4j
public class UserTokenFilter extends AbstractGatewayFilterFactory<UserTokenFilter.FilterConfig> {

    private static final String ACCESS_TOKEN = "access_token";

    /** 同步式客户端 */
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    /** 响应式客户端 */
    @Autowired
    ReactiveRedisOperations<String, String> reactiveRedisTemplate;

    @Autowired
    public UserTokenFilter() {
        super(FilterConfig.class);
    }

    @Override
    public GatewayFilter apply(FilterConfig config) {
        // return new FilterChain();
        return (exchange, chain) -> {
            // 判断是否要启动拦截
            if (!config.isEnabledFilter()) {
                return chain.filter(exchange);
            }
            ServerHttpRequest request = exchange.getRequest();
            String accessToken = request.getHeaders().getFirst(ACCESS_TOKEN);
            ServerHttpResponse response = exchange.getResponse();

            if (accessToken == null) {
                return this.unauthorizedResp(response, "access_token参数为空");
            }
            Mono<String> monoRes = this.getUserId(accessToken);
            return monoRes.map(Optional::of).defaultIfEmpty(Optional.empty()).flatMap(optional -> {
                if (!optional.isPresent()) {
                    return this.unauthorizedResp(response, "校验失败，请重试！");
                }
                String userId = optional.get();
                exchange.getRequest().mutate().header("userId", userId).build();
                return chain.filter(exchange.mutate().request(request).build());
            });
        };

    }

    private Mono<String> getUserId(String accessToken) {
        // 这种同步代码的写法是有问题的，即便它执行很快，连1ms都不需要，但是依然不行，因为网关线程总数只有几个，应该使用响应式编程
        // String userId = redisTemplate.opsForValue().get(accessToken); // 0.5ms 2000, 16000
        // return Mono.just(userId);
        return reactiveRedisTemplate.opsForValue().get(accessToken);
    }

    /**
     * 使用线程池去执行
     * 
     * @param accessToken
     * @return
     */
    public Mono<String> getUserIdInThread(String accessToken) {
        return Mono.fromFuture(MyThreadPool.supplyAsync(() -> {
            String userId = redisTemplate.opsForValue().get(accessToken);
            return userId;
        }));
    }

    /**
     * 返回认证失败
     * 
     * @param response
     * @return
     */
    private Mono<Void> unauthorizedResp(ServerHttpResponse response, String msg) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("code", HttpStatus.UNAUTHORIZED.value());
        byte[] data = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(data);
        Mono<Void> mono = response.writeWith(Mono.just(buffer));
        return mono;
    }

    /**
     * 过滤器配置
     */
    @Setter
    @Getter
    public static class FilterConfig {
        /**
         * 是否启动认证
         */
        private boolean enabledFilter;
    }

}
