package com.helloworld.webflux;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * @author doctor
 * 
 */
@SpringBootApplication
public class WebFluxServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebFluxServerApplication.class, args);
    }
}

@Configuration
class TestRouter {

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route(POST("/user/test").and(accept(MediaType.APPLICATION_JSON)), (request) -> {
            System.out.println("------test----- " + System.currentTimeMillis());
            return ServerResponse.ok().body(Mono.just("user-test"), String.class);
        });
    }
}

@RestController()
@RequestMapping(value = "/")
class WebController {

    @Autowired
    private PostRepository posts;

    @RequestMapping("/test/{time}")
    public Mono<Map<String,String>> test(@PathVariable("time") int time) {
        posts.findById("test");
        Map<String,String> map = new HashMap<>(1024);
        map.put("key", "value");
        return Mono.just(map);
    }
     
}
