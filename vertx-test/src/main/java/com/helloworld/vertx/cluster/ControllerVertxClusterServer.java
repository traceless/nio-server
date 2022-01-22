package com.helloworld.vertx.cluster;

import com.hazelcast.config.Config;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * 前端路由控制controller，也可做网关
 * 
 * @author zyj
 */
public class ControllerVertxClusterServer {

    public static void main(String[] args) {
        Config hazelcastConfig = new Config();
        hazelcastConfig.getNetworkConfig().setPort(5702);
        ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                System.out.println("VertxClusterController start success");
                vertx.deployVerticle(ControllerVerticle.class.getName());
            } else {
                // failed!
                System.out.println("VertxClusterController start failed");
            }
        });
    }

    public static class ControllerVerticle extends AbstractVerticle {

        @Override
        public void start() throws Exception {
            System.out.println("---ControllerVerticle start ---");
            // 设置数据量的大小，在数据量小的时候，这个值可以不用设置
            HttpServerOptions httpopt = new HttpServerOptions().setMaxWebSocketFrameSize(1234567);
            HttpServer server = vertx.createHttpServer(httpopt);
            Router router = Router.router(vertx);
            router.route("/order").handler(ctx -> {
                System.out.println("---test----" + System.currentTimeMillis());
                vertx.eventBus().request(OrderVertxClusterServer.ORDER_SERVER_ADDRESS, "client order message", resp ->{
                    // 拿到user服务的响应值
                    ctx.response().end(resp.result().body().toString());
                });
            });
            router.route("/user").handler(ctx -> {
                System.out.println("---test----" + System.currentTimeMillis());
                vertx.eventBus().request(UserVertxClusterServer.USER_SERVER_ADDRESS, "client user message", resp ->{
                    // 拿到user服务的响应值
                    ctx.response().end(resp.result().body().toString());
                });
            });
            server.requestHandler(router).listen(8080, "0.0.0.0");
        }

    }

}
