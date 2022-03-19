package com.helloworld.springboot;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.helloworld.springboot.model.Goods;
import com.helloworld.springboot.model.Order;

import org.springframework.stereotype.Service;

@Service
public class OrderServiceHandle {

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
    public void submitOrder(long userId, long goodsId, long skuId, BiConsumer<Order, Exception> consumer)
            throws Exception {
        getGoodsInfo(goodsId, (goods) -> {
            if (goods == null) {
                Exception exception = new Exception("error goods");
                consumer.accept(null, exception);
            }
            getLevel(userId, (level) -> {
                if (level > 0) {
                    saveOrder(userId, goods, (order) -> {
                        consumer.accept(order, null);
                        return "ok";
                    });
                }
            });
        });
    }

    public void getGoodsInfo(long goodsId, HandleGoods consumer) {
        Goods goods = new Goods(goodsId, "apple");
        consumer.handle(goods);
    }

    public void getLevel(long userId, Consumer<Integer> consumer) {
        consumer.accept(2);
    }

    public void saveOrder(long userId, Goods goods, Function<Order, String> function) {
        Order order = new Order(123L, 456L);
        String msg = function.apply(order);
        System.out.println("other msg: " + msg);
    }

}

/**
 * 其实就是Consumer的一种函数，这里表示可以自定义函数（方法）的参数
 */
@FunctionalInterface
interface HandleGoods {

    void handle(Goods goods);

}
