package com.mobigen.sqlgen.list;

public class QueryExpressionDSL<T> {
    private SqlTable table;
    private QueryExpressionDSL(FromGatherer<T> fromGatherer, SqlTable table) {
        this.table = table;

    }

    public static class FromGatherer<T> {


        private FromGatherer(Builder<T> builder) {

        }

        public QueryExpressionDSL<T> from(SqlTable table) {
            return new QueryExpressionDSL<>(this, table);
        }

        public static class Builder<T> {
            public FromGatherer<T> build() {
                return new FromGatherer<>(this);
            }
        }
    }

}
