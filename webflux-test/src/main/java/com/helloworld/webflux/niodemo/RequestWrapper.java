package com.helloworld.webflux.niodemo;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import lombok.Data;

/**
 * 传输对象
 * @author doctor
 * @since 2022/2/22 14:32
 */
@Data
public class RequestWrapper {

    private String reqData;

    private SocketChannel socketChannel;

    private SelectionKey selectionKey;

    private boolean async = false;

    public RequestWrapper() {

    }

    public RequestWrapper(String reqData, SelectionKey selectionKey) {
        this.reqData = reqData;
        this.selectionKey = selectionKey;
        this.socketChannel = (SocketChannel) selectionKey.channel();
    }
}
