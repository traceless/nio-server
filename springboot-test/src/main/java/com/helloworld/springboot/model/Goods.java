package com.helloworld.springboot.model;

import lombok.Data;

@Data
public class Goods {

    public Goods(long goodsId, String goodsName) {
        this.goodsId = goodsId;
        this.goodsName = goodsName;
    }

    public long goodsId;

    public String goodsName;

    public int price = 100;

}
