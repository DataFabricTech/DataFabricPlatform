package com.mobigen.sqlgen.where.conditions;

import com.mobigen.sqlgen.where.AbstractBinaryCondition;

public class GreaterThanEqual<L, R> extends AbstractBinaryCondition<L, R> {
    private GreaterThanEqual(L left, R right) {
        super(left, right);
    }

    @Override
    public String operator() {
        return ">=";
    }

    public static <L, R> GreaterThanEqual<L, R> of(L left, R right) {
        return new GreaterThanEqual<>(left, right);
    }
}
