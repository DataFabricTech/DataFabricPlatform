package com.mobigen.sqlgen.maker.generate;

import com.mobigen.sqlgen.maker.JoinHow;
import com.mobigen.sqlgen.maker.QueryMaker;
import com.mobigen.sqlgen.maker.where.Condition;
import com.mobigen.sqlgen.model.SqlTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JoinStatementProvider implements StatementProvider {
    private final StatementProvider statementProvider;
    private final String rightTable;
    private final String how;
    private final String whereStatement;

    private JoinStatementProvider(Builder builder) {
        statementProvider = Objects.requireNonNull(builder.statementProvider);
        rightTable = Objects.requireNonNull(builder.rightTable);
        how = builder.how;
        whereStatement = builder.whereStatement;
    }

    @Override
    public String getStatement() {
        var statement = statementProvider.getStatement();
        statement = String.format("%s %s %s", statement, how, rightTable);

        if (whereStatement.strip().isBlank()) {
            return statement;
        } else {
            return statement + " on " + whereStatement;
        }
    }

    public static class Builder {
        private StatementProvider statementProvider;
        private String rightTable;
        private String how;
        private String whereStatement;

        public Builder withSelectStatementProvider(StatementProvider statementProvider) {
            this.statementProvider = statementProvider;
            return this;
        }

        public Builder withTable(SqlTable rightTable) {
            this.rightTable = rightTable.getTotalName();
            return this;
        }

        public Builder withHow(JoinHow how) {
            this.how = how.getValue();
            return this;
        }

        public Builder withConditions(List<Condition> conditions) {
            this.whereStatement = conditions.stream()
                    .map(Condition::getStatement)
                    .collect(Collectors.joining(" and "));
            return this;
        }

        public JoinStatementProvider build() {
            return new JoinStatementProvider(this);
        }
    }

}
