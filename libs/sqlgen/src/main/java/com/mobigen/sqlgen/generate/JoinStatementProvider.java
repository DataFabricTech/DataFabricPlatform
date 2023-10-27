package com.mobigen.sqlgen.generate;

import com.mobigen.sqlgen.model.JoinMethod;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.where.Condition;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SQL 의 join ... (on ...)? 을 생성하는 클래스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
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

        public Builder withStatementProvider(StatementProvider statementProvider) {
            this.statementProvider = statementProvider;
            return this;
        }

        public Builder withTable(SqlTable rightTable) {
            this.rightTable = rightTable.getTotalName();
            return this;
        }

        public Builder withHow(JoinMethod how) {
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
