package com.helloworld.vertx.cluster;

import com.hazelcast.config.Config;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * ORDER 服务
 * @author zyj
 */
public class OrderVertxClusterServer {
    
    public static void main(String[] args) {
        Config hazelcastConfig = new Config();
        // 如果有端口冲突，会自动曾加端口号，可能会自动变成 5705.
        hazelcastConfig.getNetworkConfig().setPort(5704);
        ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                System.out.println("OrderVertxClusterServer start success");
                vertx.deployVerticle(OrderVertxClusterServerVerticle.class.getName());
            } else {
                // failed!
                System.out.println("OrderVertxClusterServer start failed");
            }
        });
    }

    public static final String ORDER_SERVER_ADDRESS = "ORDER_SERVER_ADDRESS";

    public static class OrderVertxClusterServerVerticle extends AbstractVerticle {

        @Override
        public void start() throws Exception {
            System.out.println("---OrderVertxClusterServer start ---");
            vertx.eventBus().consumer(ORDER_SERVER_ADDRESS, req ->{
                UserInfo userInfo= new UserInfo();
                userInfo.setName("name");
                System.out.println("---order come in ---");
                req.reply("i'm from order msg ");
            });
        }
    
    }
}
