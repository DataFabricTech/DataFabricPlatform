package com.mobigen.sqlgen.maker.where;

public interface Condition {
    String operator();

    String getStatement();
}
