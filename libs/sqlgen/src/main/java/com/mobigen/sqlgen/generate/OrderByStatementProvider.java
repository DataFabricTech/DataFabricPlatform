package com.mobigen.sqlgen.generate;

import com.mobigen.sqlgen.order.Order;
import com.mobigen.sqlgen.where.Condition;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL 의 where ... 을 생성하는 클래스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class OrderByStatementProvider implements StatementProvider {
    private final StatementProvider statementProvider;
    private final String orderByStatement;

    private OrderByStatementProvider(Builder builder) {
        statementProvider = builder.statementProvider;
        orderByStatement = builder.orderByStatement;
    }

    @Override
    public String getStatement() {
        var statement = statementProvider.getStatement();
        if (orderByStatement.strip().isBlank()) {
            return statement;
        } else {
            return statement + " order by " + orderByStatement;
        }
    }

    public static class Builder {
        private StatementProvider statementProvider;
        private String orderByStatement;

        public Builder withStatementProvider(StatementProvider statementProvider) {
            this.statementProvider = statementProvider;
            return this;
        }

        public Builder withOrder(List<Order> orderList) {
            this.orderByStatement = orderList.stream()
                    .sorted()
                    .map(Order::getStatement)
                    .collect(Collectors.joining(", "));
            return this;
        }

        public OrderByStatementProvider build() {
            return new OrderByStatementProvider(this);
        }
    }

}
