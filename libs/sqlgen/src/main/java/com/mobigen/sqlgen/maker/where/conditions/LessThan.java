package com.mobigen.sqlgen.maker.where.conditions;

import com.mobigen.sqlgen.maker.where.AbstractBinaryCondition;

public class LessThan<L, R> extends AbstractBinaryCondition<L, R> {
    private LessThan(L left, R right) {
        super(left, right);
    }

    @Override
    public String operator() {
        return "<";
    }

    public static <L, R> LessThan<L, R> of(L left, R right) {
        return new LessThan<>(left, right);
    }
}
