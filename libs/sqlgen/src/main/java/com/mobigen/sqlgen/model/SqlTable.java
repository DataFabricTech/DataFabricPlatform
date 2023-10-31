package com.mobigen.sqlgen.model;

import java.util.Objects;

/**
 * SQL 생성시 table 지정
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class SqlTable {
    private final String name;
    private String alias;
    private final Character stringSpecialChar = '"';

    public String getAlias() {
        return stringSpecialChar + alias + stringSpecialChar;
    }

    public String getTotalName() {
        if (name.equals(alias)) {
            return stringSpecialChar + name + stringSpecialChar;
        }
        return stringSpecialChar + name + stringSpecialChar + " as " + getAlias();
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
