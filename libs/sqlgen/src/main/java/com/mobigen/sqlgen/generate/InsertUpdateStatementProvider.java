package com.mobigen.sqlgen.generate;


import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.model.SqlValue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InsertUpdateStatementProvider implements StatementProvider {
    private final Boolean isInsert;
    private final String tableStatement;
    private final String columnStatement;
    private final String valuesStatement;

    private InsertUpdateStatementProvider(Builder builder) {
        this.isInsert = builder.isInsert;
        this.tableStatement = Objects.requireNonNull(builder.tableStatement);
        this.columnStatement = builder.columnStatement;
        this.valuesStatement = Objects.requireNonNull(builder.valuesStatement);
    }

    @Override
    public String getStatement() {
        String statement;
        if (isInsert) {
            statement = "insert into " + tableStatement;
            if (columnStatement != null && !columnStatement.isBlank()) {
                statement = statement + "(" + columnStatement + ")";
            }
            statement = statement + " values " + valuesStatement;
        } else { // update
            statement = "update " + tableStatement + " set ";
            if (columnStatement == null || columnStatement.isBlank()) {
                throw new RuntimeException("Column must be set.");
            }
            statement = statement + "(" + columnStatement + ") = "
                    + valuesStatement;
        }
        return statement;
    }

    public static class Builder {
        private final Boolean isInsert;
        private String tableStatement;
        private String columnStatement;
        private String valuesStatement;

        public Builder(Boolean isInsert) {
            this.isInsert = isInsert;
        }

        public Builder withTable(SqlTable table) {
            this.tableStatement = table.getTotalName();
            return this;
        }

        public Builder withColumns(List<SqlColumn> insertColumns) {
            this.columnStatement = insertColumns.stream()
                    .map(SqlColumn::getNameWithSpecialChar)
                    .collect(Collectors.joining(", "));
            return this;
        }

        public Builder withValues(List<List<Object>> insertValues) {
            if (isInsert) {
                this.valuesStatement = insertValues.stream()
                        .map(x -> new SqlValue<>(x).getValue())
                        .collect(Collectors.joining(", "));
            } else {
                this.valuesStatement = new SqlValue<>(insertValues.get(0)).getValue();
            }
            return this;
        }

        public InsertUpdateStatementProvider build() {
            return new InsertUpdateStatementProvider(this);
        }
    }

}
