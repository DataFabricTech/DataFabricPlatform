package com.mobigen.sqlgen.maker.where;

import com.mobigen.sqlgen.model.SqlColumn;

public class ConditionValue<T> {
    T value;

    protected ConditionValue(T value) {
        this.value = value;
    }

    protected String getValue() {
        switch (value.getClass().getSimpleName()) {
            case "SqlColumn" -> {
                return ((SqlColumn) value).getName();
            }
            case "String" -> {
                return String.format("'%s'", value);
            }
            default -> {  // "Integer", "Float", "Double"
                return String.format("%s", value);
            }
        }
    }
}
