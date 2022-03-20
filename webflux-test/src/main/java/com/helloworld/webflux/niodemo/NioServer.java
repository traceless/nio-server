package com.helloworld.webflux.niodemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.function.Function;

import org.springframework.util.StringUtils;

/**
 * 服务类
 * 
 * @author doctor
 * @since 2022/2/22 14:32
 */
public class NioServer {

    private ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
    private Selector selector;
    private Function<RequestWrapper, String> handleRequest;

    public NioServer(int port, Function<RequestWrapper, String> handle) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));
        System.out.println("listening on port " + port);
        // 处理客户端请求的函数
        this.handleRequest = handle;
        this.selector = Selector.open();
        // 绑定channel的accept
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void eventPoll() throws Exception {
        // block api
        while (selector.select() > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                // 新连接
                if (selectionKey.isAcceptable()) {
                    System.out.println("isAcceptable");
                    ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                    // 新注册channel
                    SocketChannel socketChannel = server.accept();
                    if (socketChannel == null) {
                        continue;
                    }
                    socketChannel.configureBlocking(false);
                    // 注意！这里和阻塞io的区别非常大，在编码层面之前的等待输入已经变成了注册事件，这样我们就可以在等待的时候做别的事情，
                    socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                }

                // 服务端关心的可读，意味着有数据从client传来了，根据不同的需要进行读取，然后返回
                if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    readBuffer.clear();
                    int recount = socketChannel.read(readBuffer);
                    if (recount == -1) {
                        System.out.println("==browser was close connect");
                        socketChannel.close();
                        selectionKey.cancel();
                        continue;
                    }
                    readBuffer.flip();
                    String receiveData = Charset.forName("UTF-8").decode(readBuffer).toString();
                    if (StringUtils.isEmpty(receiveData)) {
                        continue;
                    }
                    System.out.println("==receive browser reqData:" + receiveData);
                    // 执行handleRequest 函数，然后把结果响应给客户端
                    RequestWrapper wrapper = new RequestWrapper(receiveData, selectionKey);
                    String respData = handleRequest.apply(wrapper);
                    // 如果为true就写入，否则就写回给浏览器客户端，默认同步
                    if (!wrapper.isAsync()) {
                        if (respData == null) {
                            respData = "system error";
                        }
                        System.out.println("==sync respData:" + respData);
                        socketChannel.write(ByteBuffer.wrap(respData.getBytes()));
                    }
                }
                // 实际上服务端不在意这个事件，服务端只关注来之客户端的请求数据事件
                if (selectionKey.isWritable()) {
                    // String respData = (String) selectionKey.attachment();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new NioServer(8080, reqWrapper -> {
            String reqBody = reqWrapper.getReqData();
            StringBuilder respData = new StringBuilder();
            respData.append("HTTP/1.1 200 Ok\r\n");
            respData.append("Content-Type:text/html;charset=utf-8\r\n");
            respData.append("Content-Length: 4\r\n");
            respData.append("\r\n");
            respData.append("test");
            try {
                reqWrapper.setAsync(true);
                System.out.println("== server reps \r\n" + respData);
                reqWrapper.getSocketChannel().write(ByteBuffer.wrap(respData.toString().getBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return respData.toString();
        }).eventPoll();
    }
}
