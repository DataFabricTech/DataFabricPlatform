package com.mobigen.sqlgen.model;

import java.sql.JDBCType;
import java.util.Objects;

/**
 * SQL 의 테이블의 컬럼
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class SqlColumn {
    private final String name;
    private final SqlTable table;
    private final JDBCType type;

    public String getNameWithTable() {
        return table.getAlias() + "." + getNameWithSpecialChar();
    }

    public String getNameWithSpecialChar() {
        Character stringSpecialChar = '"';
        return stringSpecialChar + name + stringSpecialChar;
    }

    private SqlColumn(Builder builder) {
        name = Objects.requireNonNull(builder.name);
        table = Objects.requireNonNull(builder.table);
        type = builder.type;

    }

    public static SqlColumn of(String name, SqlTable table) {
        return new SqlColumn.Builder()
                .withName(name)
                .withTable(table)
                .build();
    }

    public static SqlColumn of(String name, SqlTable table, JDBCType type) {
        return new SqlColumn.Builder()
                .withName(name)
                .withTable(table)
                .withType(type)
                .build();
    }

    private static class Builder {
        private String name;
        private SqlTable table;
        private JDBCType type;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withTable(SqlTable table) {
            this.table = table;
            return this;
        }

        public Builder withType(JDBCType type) {
            this.type = type;
            return this;
        }

        public SqlColumn build() {
            return new SqlColumn(this);
        }
    }
}
