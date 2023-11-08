package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.generate.OrderByStatementProvider;
import com.mobigen.sqlgen.generate.StatementProvider;
import com.mobigen.sqlgen.order.Order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class OrderByMaker implements MakerInterface{
    private final MakerInterface maker;
    private List<Order> orderList;

    private OrderByMaker(OrderByMaker.Builder builder) {
        maker = builder.maker;
        orderList = builder.orderList;
    }

    @Override
    public StatementProvider generate() {
        return new OrderByStatementProvider.Builder()
                .withStatementProvider(maker.generate())
                .withOrder(orderList)
                .build();
    }

    protected static class Builder {
        private MakerInterface maker;
        private final List<Order> orderList = new ArrayList<>();

        protected OrderByMaker.Builder withConditions(Collection<? extends Order> orderList) {
            this.orderList.addAll(orderList);
            return this;
        }

        public OrderByMaker.Builder withMaker(MakerInterface maker) {
            this.maker = maker;
            return this;
        }

        protected OrderByMaker build() {
            return new OrderByMaker(this);
        }
    }
}
