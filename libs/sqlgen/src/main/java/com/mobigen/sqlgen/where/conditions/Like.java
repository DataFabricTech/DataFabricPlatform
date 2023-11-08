package com.mobigen.sqlgen.where.conditions;

import com.mobigen.sqlgen.where.AbstractBinaryCondition;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class Like<L, R> extends AbstractBinaryCondition<L, R> {
    private Like(L left, R right) {
        super(left, right);
    }

    @Override
    public String operator() {
        return "like";
    }

    public static <L, R> Like<L, R> of(L left, R right) {
        return new Like<>(left, right);
    }
}
