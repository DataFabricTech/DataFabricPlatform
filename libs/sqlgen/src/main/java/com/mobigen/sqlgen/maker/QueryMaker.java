package com.mobigen.sqlgen.maker;


import com.mobigen.sqlgen.SqlColumn;
import com.mobigen.sqlgen.SqlTable;
import com.mobigen.sqlgen.maker.where.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryMaker implements MakerInterface {
    private SqlTable table;
    private final List<SqlColumn> selectColumns;


    private QueryMaker(Builder builder) {
        this.selectColumns = builder.selectColumns;
    }

    public static QueryMaker select(SqlColumn... columns) {
        return new QueryMaker.Builder()
                .withSelectColumns(List.of(columns))
                .build();
    }

    public QueryMaker from(SqlTable table) {
        this.table = table;
        return this;
    }

    public WhereMaker where(Condition... conditions) {
        return new WhereMaker.Builder()
                .withQueryMaker(this)
                .withConditions(List.of(conditions))
                .build();
    }

    @Override
    public SelectStatementProvider generate() {
        return new SelectStatementProvider.Builder()
                .withSelectColumns(selectColumns)
                .withTable(table)
                .build();
    }


    public static class Builder {
        private final List<SqlColumn> selectColumns = new ArrayList<>();

        protected Builder withSelectColumns(Collection<? extends SqlColumn> columns) {
            this.selectColumns.addAll(columns);
            return this;
        }


        protected QueryMaker build() {
            return new QueryMaker(this);
        }
    }
}
