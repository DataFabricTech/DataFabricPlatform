package com.mobigen.sqlgen.where.conditions;

import com.mobigen.sqlgen.where.AbstractBinaryCondition;

public class Or<L, R> extends AbstractBinaryCondition<L, R> {
    private Or(L left, R right) {
        super(left, right);
    }

    @Override
    public String operator() {
        return "or";
    }

    @Override
    public String getStatement() {
        return String.format("(%s %s %s)", getLeft(), operator(), getRight());
    }

    public static <L, R> Or<L, R> of(L left, R right) {
        return new Or<>(left, right);
    }
}
