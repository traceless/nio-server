package com.helloworld.vertx.cluster;

import java.io.Serializable;

import com.hazelcast.config.Config;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.Data;

/**
 * USER 服务
 * @author zyj
 */
public class UserVertxClusterServer {
    
    public static void main(String[] args) {
        Config hazelcastConfig = new Config();
        hazelcastConfig.getNetworkConfig().setPort(5703);
        ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                System.out.println("UserVertxClusterServer start success");
                vertx.deployVerticle(UserVertxClusterServerVerticle.class.getName());
            } else {
                // failed!
                System.out.println("UserVertxClusterServer start failed");
            }
        });
    }

    public static final String USER_SERVER_ADDRESS = "USER_SERVER_ADDRESS";

    public static class UserVertxClusterServerVerticle extends AbstractVerticle {

        @Override
        public void start() throws Exception {
            System.out.println("---UserVertxClusterServer start ---");
            vertx.eventBus().consumer(USER_SERVER_ADDRESS, req ->{
                UserInfo userInfo= new UserInfo();
                userInfo.setName("name");
                System.out.println("---order come in ---");
                req.reply("i'm from user msg");
            });
        }
    }
}

@Data
class UserInfo implements Serializable{
    private String name;
    private String passwd;
}