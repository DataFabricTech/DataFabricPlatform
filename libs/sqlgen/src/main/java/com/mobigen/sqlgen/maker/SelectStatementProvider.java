package com.mobigen.sqlgen.maker;


import com.mobigen.sqlgen.SqlColumn;
import com.mobigen.sqlgen.SqlTable;

import java.util.List;
import java.util.stream.Collectors;

public class SelectStatementProvider implements StatementProvider {
    private String selectStatement;
    private String tableStatement;

    private SelectStatementProvider(Builder builder) {
        this.selectStatement = builder.selectStatement;
        this.tableStatement = builder.tableStatement;
    }

    @Override
    public String getStatement() {
        return "select " + selectStatement
                + " from " + tableStatement;
    }

    protected static class Builder {
        private String selectStatement;
        private String tableStatement;

        protected Builder withSelectColumns(List<SqlColumn> selectColumns) {
            this.selectStatement = selectColumns.stream()
                    .map(SqlColumn::getName)
                    .collect(Collectors.joining(", "));
            return this;
        }

        protected Builder withTable(SqlTable table) {
            this.tableStatement = table.getName();
            return this;
        }

        protected SelectStatementProvider build() {
            return new SelectStatementProvider(this);
        }
    }

}
