package com.helloworld.webflux.niodemo;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 测试服务
 * @author doctor
 * @since 2022/2/22 14:32
 */
public class NioServerDemo {

    public static void main(String[] args) throws Exception {
        // 运行NIO客户端，请求nodejs服务，类比rpc，200个连接
        NioClient client = new NioClient("127.0.0.1", 7080, 200);
        
        // 启动服务监听 8080 监听。设置handleRequest函数
        NioServer server = new NioServer(8080, (reqWrapper) -> {
            System.out.println("==Thread currentThread name: " + Thread.currentThread().getName());
            // 设置为异步返回，否则此次执行完就会直接返回信息给客户端。
            reqWrapper.setAsync(true);
            // 处理浏览器客户端请求的数据，把浏览器客户端的请求转发到nodejs服务中。
            String reqData = reqWrapper.getReqData();
            client.sendMsg(reqData, (resp) -> {
                // 来之客户端的线程，它来把来之远程服务端的数据，写入到SocketChannel
                System.out.println("==client Thread name：" + Thread.currentThread().getName() + " response: " + resp);
                try {
                    reqWrapper.getSocketChannel().write(ByteBuffer.wrap(resp.getBytes()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            // 返回无效，setAsync 设置为异步
            return "resp";
        });
        // 由主线程监听即可，也可以新起一个线程去处理，可以自行测试。注意当前eventPoll() 所在的线程是main。
        server.eventPoll();
    }
}
