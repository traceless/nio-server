package com.helloworld.webflux.niodemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.springframework.util.StringUtils;

import lombok.Data;

/**
 * client 端
 *
 * @author doctor
 * @since 2022/2/22 15:10
 */
public class NioClient {

    private String host = "127.0.0.1";
    private int port = 8080;
    private int pool = 20;
    private final ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
    private static Selector selector;
    private ConcurrentLinkedQueue<RequesInfo> reqDeque = new ConcurrentLinkedQueue<>();

    public NioClient(String host, int port) throws IOException {
        init(host, port, 1);
    }

    public NioClient(String host, int port, int pool) throws IOException {
        init(host, port, pool);
    }

    /**
     * 初始化连接
     * 
     * @param host
     * @param port
     * @param pool
     * @throws IOException
     */
    private void init(String host, int port, int pool) throws IOException {
        this.host = host;
        this.port = port;
        this.pool = pool;
        synchronized (NioClient.class) {
            if (selector == null) {
                selector = Selector.open();
            }
        }
        // 设置监听的连接数
        for (int i = 0; i < pool; i++) {
            createConnect();
        }
        System.out.println("与服务器的连接建立成功数量:" + pool);
        // 然后创建一个线程，用于执行 eventPoll 轮训
        Thread thread = new Thread(() -> {
            try {
                eventPoll();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setName("nioclient_thread");
        thread.start();
    }

    private void createConnect() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    /**
     * 一直轮训获取数据, 默认init的时候启动一个线程完成监听轮训select()
     * 
     * @throws IOException
     */
    private void eventPoll() throws IOException {
        while (selector.select() > 0) {

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                // 接收来之服务端的响应
                if (key.isReadable()) {
                    receive(key);
                    continue;
                }
                if (key.isWritable()) {
                    send(key);
                    continue;
                }
            }

            Set<SelectionKey> set = selector.keys();
            if (set.size() < pool) {
                // 有客户端连接被断开了，需要重新连接上
                System.out.println("==do createConnect");
                // 可以使用线程异步处理
                createConnect();
            }
        }
    }

    /**
     * 发送请求
     * 
     * @param key
     * @throws IOException
     */
    private void send(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        // 判断当前的socket是否已经请求过数据，且没有得到服务器的响应
        RequesInfo lastReqData = (RequesInfo) key.attachment();
        // 判断socket不能写数据，http协议中需要等待服务器的响应完或者超时，才能发起下一次请求
        if (lastReqData != null && System.currentTimeMillis() - lastReqData.getCreateTime() < 3000) {
            // System.out.println("this key can't write reqData, " + key.hashCode());
            return;
        }
        // 如果当前socket可以写入数据，那么拉取队列中的请求的数据
        RequesInfo info = reqDeque.poll();
        if (info == null) {
            return;
        }
        String reqData = info.getReqData();
        if (StringUtils.isEmpty(reqData)) {
            return;
        }
        System.out.println("==this nioClient send reqData：" + (reqData.length() < 10 ? reqData : ""));
        socketChannel.write((ByteBuffer.wrap(reqData.getBytes())));
        // 绑定请求数据用于下次判断
        key.attach(info);
    }

    /**
     * 接收来之服务器的响应
     * 
     * @param key
     * @throws IOException
     */
    private void receive(SelectionKey key) throws IOException {
        // 执行 resposeHandle
        RequesInfo requesInfo = (RequesInfo) key.attachment();
        // 清空信息，让当前的socket 可以进行下次请求发送，在return前清理掉，避免bug
        key.attach(null);
        // 获取数据
        SocketChannel socketChannel = (SocketChannel) key.channel();
        receiveBuffer.clear();
        int reconut = socketChannel.read(receiveBuffer);
        if (reconut == -1) {
            System.out.println("==remoteserver was close connect skey: " + key.hashCode());
            key.cancel();
            socketChannel.close();
            return;
        }
        receiveBuffer.flip();
        String receiveData = Charset.forName("UTF-8").decode(receiveBuffer).toString();
        if (StringUtils.isEmpty(receiveData)) {
            return;
        }
        System.out.println("==this nioClient receive:" + (receiveData.length() < 30 ? receiveData : ""));
        if (requesInfo != null) {
            // 执行nioclient的 ResposeHandle 函数
            requesInfo.getResposeHandle().accept(receiveData);
        }
    }

    /**
     * 扔进队列，等待有可写的socker，然后发送
     * 
     * @param reqMsg
     * @param handle
     */
    public void sendMsg(String reqMsg, Consumer<String> handle) {
        reqDeque.add(new RequesInfo(reqMsg, handle));
    }

    @Data
    class RequesInfo {
        private String reqData;
        private long createTime;
        private Consumer<String> resposeHandle;

        public RequesInfo(String reqData, Consumer<String> resposeHandle) {
            this.reqData = reqData;
            this.resposeHandle = resposeHandle;
            this.createTime = System.currentTimeMillis();
        }
    }

    public static void main(String[] args) throws Exception {
        final NioClient client = new NioClient("127.0.0.1", 8080, 22);
        // 从键盘上获取输入信息，会被发送到服务端
        Thread receiver = new Thread(() -> {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            try {
                String msg;
                while ((msg = bufferedReader.readLine()) != null) {
                    // 发送到服务端
                    client.sendMsg(msg + "\r\n", resp -> {
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        receiver.start();
    }
}