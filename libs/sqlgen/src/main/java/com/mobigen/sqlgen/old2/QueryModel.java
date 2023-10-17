package com.mobigen.sqlgen.old2;

import com.mobigen.sqlgen.model.SqlColumn;

import java.util.List;

public class QueryModel {
    private List<SqlColumn> selectColumns;
    private QueryModel(Builder builder) {
        this.selectColumns = builder.selectColumns;
    }

    protected static class Builder {
        private List<SqlColumn> selectColumns;

        public Builder withSelectColumns(List<SqlColumn> selectColumns) {
            this.selectColumns = selectColumns;
            return this;
        }
        protected QueryModel build() {
            return new QueryModel(this);
        }
    }
}
