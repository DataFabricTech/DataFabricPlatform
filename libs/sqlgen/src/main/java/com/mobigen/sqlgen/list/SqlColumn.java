package com.mobigen.sqlgen.list;

import java.sql.JDBCType;
import java.util.Objects;

public class SqlColumn {
    private String name;
    private SqlTable table;
    private JDBCType type;

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

    public static class Builder {
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