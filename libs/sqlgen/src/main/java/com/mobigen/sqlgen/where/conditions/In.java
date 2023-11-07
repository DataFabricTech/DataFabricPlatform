package com.mobigen.sqlgen.where.conditions;

import com.mobigen.sqlgen.where.AbstractBinaryCondition;

import java.util.Collection;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class In<L, R extends Collection<?>> extends AbstractBinaryCondition<L, R> {
    private In(L left, R right) {
        super(left, right);
    }

    @Override
    public String operator() {
        return "in";
    }

    public static <L, R extends Collection<?>> In<L, R> of(L left, R right) {
        return new In<>(left, right);
    }
}
