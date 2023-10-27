package com.mobigen.sqlgen.generate;

import com.mobigen.sqlgen.where.Condition;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL 의 where ... 을 생성하는 클래스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class WhereStatementProvider implements StatementProvider {
    private final StatementProvider statementProvider;
    private final String whereStatement;

    private WhereStatementProvider(Builder builder) {
        statementProvider = builder.statementProvider;
        whereStatement = builder.whereStatement;
    }

    @Override
    public String getStatement() {
        var statement = statementProvider.getStatement();
        if (whereStatement.strip().isBlank()) {
            return statement;
        } else {
            return statement + " where " + whereStatement;
        }
    }

    public static class Builder {
        private StatementProvider statementProvider;
        private String whereStatement;

        public Builder withStatementProvider(StatementProvider statementProvider) {
            this.statementProvider = statementProvider;
            return this;
        }

        public Builder withConditions(List<Condition> conditions) {
            this.whereStatement = conditions.stream()
                    .map(Condition::getStatement)
                    .collect(Collectors.joining(" and "));
            return this;
        }

        public WhereStatementProvider build() {
            return new WhereStatementProvider(this);
        }
    }

}
