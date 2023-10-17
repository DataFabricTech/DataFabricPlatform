package com.mobigen.sqlgen.old;

import com.mobigen.sqlgen.SqlColumn;
import com.mobigen.sqlgen.SqlTable;

import java.util.Collection;
import java.util.List;

public class QueryExpressionDSL<T> {
    private SqlTable table;
    private QueryExpressionDSL(FromGatherer<T> fromGatherer, SqlTable table) {
        this.table = table;

    }

    public static class FromGatherer<T> {

        private List<SqlColumn> selectList;

        private FromGatherer(Builder<T> builder) {
            this.selectList = builder.selectList;
        }

        public QueryExpressionDSL<T> from(SqlTable table) {
            return new QueryExpressionDSL<>(this, table);
        }

        public static class Builder<T> {
            private List<SqlColumn> selectList;
            public Builder<T> withColumns(Collection<? extends SqlColumn> columns) {
                this.selectList.addAll(columns);
                return this;
            }
            public FromGatherer<T> build() {
                return new FromGatherer<>(this);
            }
        }
    }

}
