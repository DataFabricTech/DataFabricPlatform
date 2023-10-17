package com.mobigen.sqlgen.generate;


import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.model.SqlValue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InsertStatementProvider implements StatementProvider {
    private final String tableStatement;
    private final String columnStatement;
    private final String valuesStatement;

    private InsertStatementProvider(Builder builder) {
        this.tableStatement = Objects.requireNonNull(builder.tableStatement);
        this.columnStatement = builder.columnStatement;
        this.valuesStatement = Objects.requireNonNull(builder.valuesStatement);
    }

    @Override
    public String getStatement() {
        var statement = "insert into " + tableStatement;
        if (columnStatement != null && !columnStatement.isBlank()) {
            statement = statement + "(" + columnStatement + ")";
        }
        statement = statement + " values " + valuesStatement;
        return statement;
    }

    public static class Builder {
        private String tableStatement;
        private String columnStatement;
        private String valuesStatement;

        public Builder withTable(SqlTable table) {
            this.tableStatement = table.getTotalName();
            return this;
        }

        public Builder withColumns(List<SqlColumn> insertColumns) {
            this.columnStatement = insertColumns.stream()
                    .map(SqlColumn::getOnlyName)
                    .collect(Collectors.joining(", "));
            return this;
        }

        public Builder withValues(List<List<Object>> insertValues) {
            this.valuesStatement = insertValues.stream()
                    .map(x -> new SqlValue<>(x).getValue())
                    .collect(Collectors.joining(", "));
            return this;
        }

        public InsertStatementProvider build() {
            return new InsertStatementProvider(this);
        }
    }

}
