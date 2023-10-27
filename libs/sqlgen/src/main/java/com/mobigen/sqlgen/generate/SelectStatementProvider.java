package com.mobigen.sqlgen.generate;


import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SQL 의 select ... (from ...)? 을 생성하는 클래스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class SelectStatementProvider implements StatementProvider {
    private final String selectStatement;
    private final String tableStatement;

    private SelectStatementProvider(Builder builder) {
        this.selectStatement = Objects.requireNonNull(builder.selectStatement);
        this.tableStatement = builder.tableStatement;
    }

    @Override
    public String getStatement() {
        var statement = "select " + selectStatement;
        if (tableStatement != null && !tableStatement.isBlank()) {
            statement = statement + " from " + tableStatement;
        }
        return statement;
    }

    public static class Builder {
        private String selectStatement;
        private String tableStatement;

        public Builder withSelectColumns(List<SqlColumn> selectColumns) {
            this.selectStatement = selectColumns.stream()
                    .map(SqlColumn::getNameWithTable)
                    .collect(Collectors.joining(", "));
            return this;
        }

        public Builder withTable(SqlTable table) {
            this.tableStatement = table.getTotalName();
            return this;
        }

        public SelectStatementProvider build() {
            return new SelectStatementProvider(this);
        }
    }

}
