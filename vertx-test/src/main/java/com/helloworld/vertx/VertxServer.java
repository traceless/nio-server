package com.helloworld.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;

/**
 * @author doctor!
 */
public class VertxServer extends AbstractVerticle {
    public static void main(String[] args) {
        System.out.println("-----starting----");
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(VertxServer.class.getName());
    }

    @Override
    public void start() throws Exception {
        HttpServerOptions httpopt = new HttpServerOptions().setMaxWebSocketFrameSize(1234567);
        HttpServer server = vertx.createHttpServer(httpopt);
        Router router = Router.router(vertx);
        router.route("/test/:time").handler(ctx -> {
            HttpServerRequest req = ctx.request();
            String time = req.getParam("time");
            System.out.println("---test---" + time);

            String url = "http://127.0.0.1:7080/test/" + time;
            VertxHttpClient.getClient().post(url, (data) -> {
                ctx.response().end("hello world vert.x" + data);
            });

        });
        server.requestHandler(router);
        server.listen(8080, "0.0.0.0");
    }
}