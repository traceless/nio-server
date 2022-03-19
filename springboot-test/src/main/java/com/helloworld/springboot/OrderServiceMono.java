package com.helloworld.springboot;

import java.util.Optional;

import com.helloworld.springboot.model.Goods;
import com.helloworld.springboot.model.Order;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class OrderServiceMono {

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
    public Mono<Order> submitOrder(long userId, long goodsId, long skuId) throws Exception {
        return getGoodsInfo(goodsId).switchIfEmpty(Mono.error(new Exception("error goods"))).flatMap(goods -> {
            return getLevel(userId).flatMap(level -> {
                if (level > 0) {
                    goods.setPrice(1);
                }
                return saveOrder(userId, goods);
            });
        });
    }

    public Mono<Order> submitOrder2(long userId, long goodsId, long skuId) throws Exception {
        return getGoodsInfo(goodsId).map(Optional::of).defaultIfEmpty(Optional.empty()).flatMap(optional ->{
            if(optional.isPresent()){
                return Mono.error(new Exception("error goods"));
            }
            Goods goods = optional.get();
            return getLevel(userId).flatMap(level -> {
                if (level > 0) {
                    goods.setPrice(1);
                }
                return saveOrder(userId, goods);
            });
        });
    }

    public Mono<Goods> getGoodsInfo(long goodsId) {
        return Mono.just(new Goods(goodsId, "apple"));
    }

    public Mono<Order> saveOrder(long userId, Goods goods) {
        return Mono.just(new Order(123L, 456L));
    }

    public Mono<Integer> getLevel(long userId) {
        return Mono.just(2);
    }
}
