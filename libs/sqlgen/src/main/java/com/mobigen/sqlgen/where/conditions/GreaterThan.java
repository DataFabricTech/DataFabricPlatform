package com.mobigen.sqlgen.where.conditions;

import com.mobigen.sqlgen.where.AbstractBinaryCondition;

public class GreaterThan<L, R> extends AbstractBinaryCondition<L, R> {
    private GreaterThan(L left, R right) {
        super(left, right);
    }

    @Override
    public String operator() {
        return ">";
    }

    public static <L, R> GreaterThan<L, R> of(L left, R right) {
        return new GreaterThan<>(left, right);
    }
}
