package com.helloworld.vertx.cluster;

import java.util.Date;

import com.hazelcast.config.Config;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * 集群管理者
 * @author zyj
 */
public class VertxClusterManager {

    public static void main(String[] args) {
        Config hazelcastConfig = new Config();
        hazelcastConfig.getNetworkConfig().setPort(5701);
        ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                EventBus eventBus = vertx.eventBus();
                eventBus.consumer("chat.to.server", message -> {
                    System.out.println(new Date()+":客户端发往服务端的消息内容为:"+message.body().toString());
                    System.out.println(new Date()+":数据发布出去的时间");
                });
                //周期性推送数据
                vertx.setPeriodic(2000, timerId -> {
                    eventBus.send("chat.to.server", "ata");
                    System.out.println(new Date()+":推送完毕");
                });
       
                vertx.setPeriodic(21000, timeId ->{
                    System.out.println("send controller");
                    vertx.eventBus().publish("controller", "message");
                });
                
                System.out.println("succeeded");
            } else {
                System.out.println("failed");
                // failed!
            }
        });
    }

}
