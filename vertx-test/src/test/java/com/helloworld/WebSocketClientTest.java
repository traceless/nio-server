package com.helloworld;

import java.util.HashMap;
import java.util.Map;

import com.helloworld.vertx.VertxHttpClient;

import io.vertx.core.json.JsonObject;

/**
 * Unit test for simple App.
 */
public class WebSocketClientTest {

    private static int num = 1;

    public static void main(String[] args) throws Exception {

        String url = "http://127.0.0.1:8080/test/20";
        // 连接数
        int poolSize = 2000;
        // 请求数量
        int sum = 22345;
        // 每次请求数量
        int step = 2000;
        VertxHttpClient client = new VertxHttpClient(poolSize, 3);
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("userId", "123456");
        System.out.println("----热身一下连接----" + poolSize);
        Map<String, String> headers = new HashMap<>(8);
        headers.put("accessToken", "123458");
        // 预热，先加载好连接
        for (int i = 0; i < poolSize; i++) {
            client.post(url, headers, jsonObject.toString(), (body) -> { });
        }
        // 发起正式请求
        Thread.sleep(2000);
        long start = System.currentTimeMillis();
        System.out.println("----开始请求----" + System.currentTimeMillis());
        for (int i = 0; i < sum; i++) {
            client.post(url, headers, jsonObject.toString(), response -> {
                if(response == null){
                    System.out.println("Something went wrong");
                }
                if (num % step == 0) {
                    System.out.println("----request sum----" + num);
                }
                if (num++ >= sum) {
                    long end = System.currentTimeMillis();
                    System.out.println("----最后一次请求响应----" + response);
                    System.out.println("----结束请求----" + end);
                    System.out.println(" 时间 " + (end - start) + " qps: " + ((sum * 1000) / (end - start)));
                }
            });
            if (i % step == 0) {
                Thread.sleep(100);
            }
        }
        
    }
 
}
