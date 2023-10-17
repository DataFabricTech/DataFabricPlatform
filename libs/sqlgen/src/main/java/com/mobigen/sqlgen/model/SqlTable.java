package com.mobigen.sqlgen.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public class SqlTable {
    private final String name;
    private String alias;

    public String getTotalName() {
        if (name.equals(alias)) {
            return name;
        }
        return name + " as " + alias;
    }

    private SqlTable(Builder builder) {
        name = Objects.requireNonNull(builder.name);
        alias = builder.alias;
        if (alias == null) {
            alias = name;
        }

    }

    public static SqlTable of(String name) {
        return new Builder()
                .withName(name)
                .build();
    }

    public static SqlTable of(String name, String alias) {
        return new Builder()
                .withName(name)
                .withAlias(alias)
                .build();
    }

    private static class Builder {
        private String name;
        private String alias;

        private Builder withName(String name) {
            this.name = name;
            return this;
        }

        private Builder withAlias(String alias) {
            this.alias = alias;
            return this;
        }

        private SqlTable build() {
            return new SqlTable(this);
        }
    }
}
