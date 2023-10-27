package com.mobigen.sqlgen.model;

import com.mobigen.sqlgen.where.Condition;

import java.util.List;
import java.util.stream.Collectors;

/**
 * value 의 타입에 맞게 SQL string 으로 변환 하기 위한 클래스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
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
