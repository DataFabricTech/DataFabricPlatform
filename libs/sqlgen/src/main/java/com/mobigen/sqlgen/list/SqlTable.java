package com.mobigen.sqlgen.list;

import java.sql.JDBCType;
import java.util.Objects;

public class SqlTable {
    private String name;

    private SqlTable(Builder builder) {
        name = Objects.requireNonNull(builder.name);

    }

    public SqlColumn column(String name, JDBCType type) {
        return SqlColumn.of(name, this, type);
    }

    public static SqlTable of(String name) {
        return new Builder()
                .withName(name)
                .build();
    }

    public static class Builder {
        private String name;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public SqlTable build() {
            return new SqlTable(this);
        }
    }
}
