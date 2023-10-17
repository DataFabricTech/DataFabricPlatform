package com.mobigen.sqlgen.old2;

import com.mobigen.sqlgen.SqlColumn;
import com.mobigen.sqlgen.SqlTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class QueryMaker<T> {
    private List<SqlColumn> selectColumns;
    private SqlTable sqlTable;
    private SelectMaker<T> selectMaker;

    private QueryMaker(FromGatherer<T> fromGatherer, SqlTable table) {
        this.selectColumns = fromGatherer.selectColumns;
        this.sqlTable = table;
        this.selectMaker = Objects.requireNonNull(fromGatherer.selectMaker);
        this.selectMaker.registerQueryMaker(this);
    }

    public QueryMaker<T> where() {
        return this;
    }

    public QueryMaker<T> groupBy() {
        return this;
    }

    public T build() {
        return selectMaker.build();
    }

    protected QueryModel buildModel() {
        var builder = new QueryModel.Builder()
                .withSelectColumns(selectColumns);
        return builder.build();
    }

    public static class FromGatherer<T> {
        private final SelectMaker<T> selectMaker;
        private final List<SqlColumn> selectColumns;

        public FromGatherer(Builder<T> builder) {
            this.selectMaker = Objects.requireNonNull(builder.selectMaker);
            this.selectColumns = builder.selectColumns;
        }

        public QueryMaker<T> from(SqlTable table) {
            return new QueryMaker<>(this, table);
        }

        public static class Builder<T> {
            private SelectModel selectModel;
            private final List<SqlColumn> selectColumns = new ArrayList<>();
            private SelectMaker<T> selectMaker;

            public Builder<T> withColumns(Collection<? extends SqlColumn> columns) {
                this.selectColumns.addAll(columns);
                return this;
            }

            public Builder<T> withSelectMaker(SelectMaker<T> selectMaker) {
                this.selectMaker = selectMaker;
                return this;
            }

            public FromGatherer<T> build() {
                return new FromGatherer<>(this);
            }
        }
    }

//    public static class Builder<T> {
//        private List<SqlColumn> selectColumns = new ArrayList<>();
//
//        public Builder<T> select(Collection<? extends SqlColumn> columns) {
//            this.selectColumns.addAll(columns);
//            return this;
//        }
//
//        public QueryMaker<T> build() {
//            return new QueryMaker<>(this);
//        }
//    }

//    public static class SelectMaker<T> {
//        public static <T> FromMaker<T> select(SqlColumn... columns) {
//            return new FromMaker.Builder<>()
//                    .withColumns(List.of(columns))
//                    .build();
//        }
//    }
//    public static class FromMaker<T> {
//        private final List<SqlColumn> selectColumns;
//        private final SelectMaker<T> selectMaker;
//
//        private FromMaker(Builder<T> builder) {
//            this.selectColumns = builder.selectColumns;
//            this.selectMaker = Objects.requireNonNull(builder.selectMaker);
//        }
//
//        public QueryMaker<T> from(SqlTable table) {
//            return new QueryMaker<>(this, table);
//        }
//
//        public static class Builder<T> {
//            private final List<SqlColumn> selectColumns = new ArrayList<>();
//            private SelectMaker<T> selectMaker;
//
//            public Builder<T> withColumns(Collection<? extends SqlColumn> columns) {
//                this.selectColumns.addAll(columns);
//                return this;
//            }
//
//            public Builder<T> withSelectMaker(SelectMaker<T> selectMaker) {
//                this.selectMaker = selectMaker;
//                return this;
//            }
//
//            public FromMaker<T> build() {
//                return new FromMaker<>(this);
//            }
//        }
//    }
}
