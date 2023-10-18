package com.mobigen.sqlgen.model;

import com.mobigen.sqlgen.where.Condition;

import java.util.List;
import java.util.stream.Collectors;

public class SqlValue<T> {
    T value;

    public SqlValue(T value) {
        this.value = value;
    }

    public String getValue() {
        if (value instanceof SqlColumn) {
            return ((SqlColumn) value).getNameWithTable();
        } else if (value instanceof List) {
            return "("
                    + ((List<?>) value).stream()
                    .map(x -> new SqlValue<>(x).getValue())
                    .collect(Collectors.joining(", "))
                    + ")";
        } else if (value instanceof String) {
            return String.format("'%s'", value);
        } else if (value instanceof Condition) {
            return ((Condition) value).getStatement();
        } else {
            return String.format("%s", value);
        }
    }
}
