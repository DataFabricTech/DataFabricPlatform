package com.mobigen.sqlgen.maker.where.conditions;

import com.mobigen.sqlgen.maker.where.AbstractBinaryCondition;

public class Equal<L, R> extends AbstractBinaryCondition<L, R> {
    private Equal(L left, R right) {
        super(left, right);
    }

    @Override
    public String operator() {
        return "=";
    }

    public static <L, R> Equal<L, R> of(L left, R right) {
        return new Equal<>(left, right);
    }
}
