package com.mobigen.sqlgen.where;

public interface Condition {
    String operator();

    String getStatement();
}
