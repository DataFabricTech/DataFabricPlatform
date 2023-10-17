package com.mobigen.sqlgen;

import java.sql.JDBCType;
import java.util.Objects;

public class SqlTable {
    private String name;

    public String getName() {
        return name;
    }

    private SqlTable(Builder builder) {
        name = Objects.requireNonNull(builder.name);

    }

    public static SqlTable of(String name) {
        return new Builder()
                .withName(name)
                .build();
    }

    private static class Builder {
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
