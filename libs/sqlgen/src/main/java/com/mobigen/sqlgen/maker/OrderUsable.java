package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.order.Order;

import java.util.List;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class OrderUsable implements MakerInterface {
    public MakerInterface orderBy(Order... orders) {
        return new OrderByMaker.Builder()
                .withMaker(this)
                .withConditions(List.of(orders))
                .build();
    }
}
