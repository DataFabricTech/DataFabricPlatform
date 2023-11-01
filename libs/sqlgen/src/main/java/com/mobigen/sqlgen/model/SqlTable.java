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
    private final Boolean caseSensitive;
    private final Character stringSpecialChar = '"';

    public String getAlias() {
        String tableAlias;
        if (!caseSensitive) {
            tableAlias = alias;
        } else {
            tableAlias = stringSpecialChar + alias + stringSpecialChar;
        }
        return tableAlias;
    }

    public String getTotalName() {
        String tableName;
        if (!caseSensitive) {
            tableName = name;
        } else {
            tableName = stringSpecialChar + name + stringSpecialChar;
        }
        if (name.equals(alias)) {
            return tableName;
        }
        return tableName + " as " + getAlias();
    }

    private SqlTable(Builder builder) {
        name = Objects.requireNonNull(builder.name);
        alias = builder.alias;
        if (alias == null) {
            alias = name;
        }
        caseSensitive = Objects.requireNonNull(builder.caseSensitive);
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

    public static SqlTable of(String name, String alias, Boolean caseSensitive) {
        return new Builder()
                .withName(name)
                .withAlias(alias)
                .withCaseSensitive(caseSensitive)
                .build();
    }

    private static class Builder {
        private String name;
        private String alias;
        private Boolean caseSensitive = false;

        private Builder withName(String name) {
            this.name = name;
            return this;
        }

        private Builder withAlias(String alias) {
            this.alias = alias;
            return this;
        }
        private Builder withCaseSensitive(Boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        private SqlTable build() {
            return new SqlTable(this);
        }
    }
}
