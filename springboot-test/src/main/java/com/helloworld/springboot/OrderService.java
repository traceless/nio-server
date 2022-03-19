package com.helloworld.springboot;

import com.helloworld.springboot.model.Goods;
import com.helloworld.springboot.model.Order;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    /**
     * 初始化
     */
    public void init() {

    }

    /**
     * 提交订单
     * 
     * @return
     * @throws Exception
     */
    public Order submitOrder(long userId, long goodsId, long skuId) throws Exception {

        // 查询商品信息，查询库存
        Goods goods = getGoodsInfo(goodsId);
        if (goods == null) {
            // 抛出异常
            throw new Exception(" goods error");
        }
        // 查询会员等级进行优惠
        int level = getLevel(userId);
        if (level > 0) {
            goods.setPrice(1);
        }
        // 提交订单
        Order order = saveOrder(userId, goods);
        return order;
    }

    public Goods getGoodsInfo(long goodsId) {
        return new Goods(goodsId, "apple");
    }

    public Order saveOrder(long userId, Goods goods) {
        return new Order(123L, 456L);
    }

    public int getLevel(long userId) {
        return 2;
    }
}
