package com.helloworld.vertx;

import java.util.Map;

import io.netty.util.internal.StringUtil;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.Data;

@Data
public class VertxHttpClient {

    private static Vertx vertx = Vertx.vertx();

    /** 连接数量 */
    private static int poolSize = 2000;

    /** 单位秒 */
    private static int timeout = 6;

    private WebClient webClient = null;

    private static VertxHttpClient vertxHttpClient = null;

    /**
     * 初始化属性
     */
    public VertxHttpClient() {
        this.init(poolSize, timeout);
    }

    public VertxHttpClient(int poolSize, int timeout) {
        this.init(poolSize, timeout);
    }

    private void init(int poolSize, int timeout){
        WebClientOptions options = new WebClientOptions();
        options.setMaxPoolSize(poolSize);
        options.setKeepAlive(true);
        options.setIdleTimeout(timeout);
        options.setMaxWaitQueueSize(50000);
        WebClient webClient = WebClient.create(vertx, options);
        this.setWebClient(webClient);
    }

    /**
     * 获取实例
     * 
     * @return
     */
    public static VertxHttpClient getClient() {
        if (vertxHttpClient != null) {
            return vertxHttpClient;
        }
        synchronized(VertxHttpClient.class){
            if(vertxHttpClient == null){
                vertxHttpClient = new VertxHttpClient();
            }
        }
        return vertxHttpClient;
    }

    /**
     * post 请求
     * @param url
     * @param handler
     */
    public void post(String url, Handler<String> handler) {
        this.post(url, null, null, 0, handler);
    }

    public void post(String url, String body, Handler<String> handler) {
        this.post(url, null, body, 0, handler);
    }

    public void post(String url, Map<String, String> headers, String body, Handler<String> handler) {
        this.post(url, headers, body, 0, handler);
    }

    public void post(String url, Map<String, String> headers, String body, int retryTimes, Handler<String> handler) {
        HttpRequest<Buffer> httpRequest = this.webClient.postAbs(url);
        if (headers != null) {
            headers.forEach(httpRequest::putHeader);
        }
        Future<HttpResponse<Buffer>> future = null;
        if (StringUtil.isNullOrEmpty(body)) {
            future = httpRequest.send();
        } else {
            JsonObject jsonObject = new JsonObject(body);
            future = httpRequest.sendJsonObject(jsonObject);
        }
        // 获取结果
        future.onSuccess(response -> {
            handler.handle(response.bodyAsString());
        }).onFailure(err -> {
            System.out.println("--err--" + err);
            if (retryTimes > 0) {
                System.out.println("--retryTimes--" + retryTimes);
                this.post(url, headers, body, retryTimes - 1, handler);
                return;
            }
            // 异常就返回null
            handler.handle(null);
        });
    }
}