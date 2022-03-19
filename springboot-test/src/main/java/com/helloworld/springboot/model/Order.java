package com.helloworld.springboot.model;

public class Order {

    public long orderId;

    public long goodsId;

    public Order(long orderId, long goodsId) {
        this.orderId = orderId;
        this.goodsId = goodsId;
    }

    public String goodsName;

}
