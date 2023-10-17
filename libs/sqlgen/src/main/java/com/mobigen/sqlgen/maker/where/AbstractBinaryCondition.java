package com.mobigen.sqlgen.maker.where;

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
        return new ConditionValue<>(left).getValue();
    }

    protected String getRight() {
        return new ConditionValue<>(right).getValue();
    }
}
