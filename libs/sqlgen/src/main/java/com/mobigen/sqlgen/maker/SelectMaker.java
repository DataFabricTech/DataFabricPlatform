package com.mobigen.sqlgen.maker;


import com.mobigen.sqlgen.generate.SelectStatementProvider;
import com.mobigen.sqlgen.model.JoinHow;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.where.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SelectMaker implements MakerInterface {
    private final SqlTable table;
    private final List<SqlColumn> selectColumns;


    private SelectMaker(Builder builder) {
        this.selectColumns = Objects.requireNonNull(builder.selectColumns);
        this.table = Objects.requireNonNull(builder.table);
    }

    public JoinMaker join(SqlTable rightTable, JoinHow how, Condition... conditions) {
        return new JoinMaker.Builder()
                .withSelectMaker(this)
                .withTable(rightTable)
                .withHow(how)
                .withConditions(List.of(conditions))
                .build();
    }

    public JoinMaker join(SqlTable rightTable, Condition... conditions) {
        return join(rightTable, JoinHow.INNER, conditions);
    }

    public JoinMaker join(SqlTable rightTable) {
        return join(rightTable, JoinHow.INNER);
    }

    public WhereMaker where(Condition... conditions) {
        return new WhereMaker.Builder()
                .withMaker(this)
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

    public static Builder select(SqlColumn... columns) {
        return new SelectMaker.Builder()
                .withSelectColumns(List.of(columns));
    }

    public static class Builder {
        private final List<SqlColumn> selectColumns = new ArrayList<>();
        private SqlTable table;

        protected Builder withSelectColumns(Collection<? extends SqlColumn> columns) {
            this.selectColumns.addAll(columns);
            return this;
        }

        public SelectMaker from(SqlTable table) {
            this.table = table;
            return build();
        }

        protected SelectMaker build() {
            return new SelectMaker(this);
        }
    }
}
