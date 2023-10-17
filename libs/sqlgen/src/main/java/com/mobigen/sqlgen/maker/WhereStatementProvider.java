package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.maker.where.Condition;

import java.util.List;
import java.util.stream.Collectors;

public class WhereStatementProvider implements StatementProvider {
    private SelectStatementProvider selectStatementProvider;
    private final String whereStatement;

    private WhereStatementProvider(Builder builder) {
        selectStatementProvider = builder.selectStatementProvider;
        whereStatement = builder.whereStatement;
    }

    @Override
    public String getStatement() {
        var statement = selectStatementProvider.getStatement();
        if (whereStatement.strip().isBlank()) {
            return statement;
        } else {
            return statement + " where " + whereStatement;
        }
    }

    protected static class Builder {
        private SelectStatementProvider selectStatementProvider;
        private String whereStatement;

        public Builder withSelectStatementProvider(SelectStatementProvider statementProvider) {
            this.selectStatementProvider = statementProvider;
            return this;
        }

        protected Builder withConditions(List<Condition> conditions) {
            this.whereStatement = conditions.stream()
                    .map(Condition::getStatement)
                    .collect(Collectors.joining(" and "));
            return this;
        }

        protected WhereStatementProvider build() {
            return new WhereStatementProvider(this);
        }
    }

}
