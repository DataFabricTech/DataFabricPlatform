package com.mobigen.sqlgen.where;

import com.mobigen.sqlgen.model.SqlValue;

/**
 * Binary 인자를 가지는 where 비교 조건
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractBinaryCondition<L, R> implements Condition {
    L left;
    R right;

    protected AbstractBinaryCondition(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String getStatement() {
        return String.format("%s %s %s", getLeft(), operator(), getRight());
    }

    protected String getLeft() {
        return new SqlValue<>(left).getValue();
    }

    protected String getRight() {
        return new SqlValue<>(right).getValue();
    }
}
