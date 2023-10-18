package com.mobigen.sqlgen.model;

import java.util.List;
import java.util.stream.Collectors;

public class SqlValue<T> {
    T value;

    public SqlValue(T value) {
        this.value = value;
    }

    public String getValue() {
        var type = value.getClass().getSimpleName();
        if (type.equals("SqlColumn")) {
            return ((SqlColumn) value).getNameWithTable();
        } else if (type.contains("List")) {
            return "("
                    + ((List<?>) value).stream()
                    .map(x -> new SqlValue<>(x).getValue())
                    .collect(Collectors.joining(", "))
                    + ")";
        } else if (type.equals("String")) {
            return String.format("'%s'", value);
        } else {
            return String.format("%s", value);
        }
    }
}
