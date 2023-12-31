package com.mobigen.sqlgen.where.conditions;

import com.mobigen.sqlgen.where.AbstractBinaryCondition;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class And<L, R> extends AbstractBinaryCondition<L, R> {
    private And(L left, R right) {
        super(left, right);
    }

    @Override
    public String operator() {
        return "and";
    }

    @Override
    public String getStatement() {
        return String.format("(%s %s %s)", getLeft(), operator(), getRight());
    }

    public static <L, R> And<L, R> of(L left, R right) {
        return new And<>(left, right);
    }
}
