package com.mobigen.sqlgen.maker.where;

import com.mobigen.sqlgen.SqlColumn;

public class Equal<L, R> extends AbstractBinaryCondition<L, R> {
    private Equal(L left, R right) {
        super(left, right);

    }

    @Override
    public String operator() {
        return "=";
    }

    private String getLeft() {
        return new ConditionValue<>(left).getValue();
    }
    private String getRight() {
        return new ConditionValue<>(right).getValue();
    }

    @Override
    public String getStatement() {
        return String.format("%s %s %s", getLeft(), operator(), getRight());
    }

    public static <L, R> Equal<L, R> of(L left, R right) {
        return new Equal<>(left, right);
    }
}
