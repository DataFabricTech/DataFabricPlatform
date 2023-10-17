package com.mobigen.sqlgen.maker.where;

import com.mobigen.sqlgen.SqlColumn;

public abstract class AbstractBinaryCondition<L, R> implements Condition {
    L left;
    R right;

    protected AbstractBinaryCondition(L left, R right) {
        this.left = left;
        this.right = right;
    }
}
