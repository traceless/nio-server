package com.helloworld.vertx.cluster;

import io.vertx.core.Vertx;

/**
 * 前端路由控制controller，也可做网关
 * @author zyj
 */
public class VertxSignleDeploy {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(ControllerVertxClusterServer.ControllerVerticle.class.getName());
        vertx.deployVerticle(UserVertxClusterServer.UserVertxClusterServerVerticle.class.getName());
        vertx.deployVerticle(OrderVertxClusterServer.OrderVertxClusterServerVerticle.class.getName());
    }
}
 