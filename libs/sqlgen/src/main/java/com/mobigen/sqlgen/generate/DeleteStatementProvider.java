package com.mobigen.sqlgen.generate;


import com.mobigen.sqlgen.model.SqlTable;

/**
 * SQL 의 delete from ... 을 생성하는 클래스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class DeleteStatementProvider implements StatementProvider {
    private final String tableStatement;

    private DeleteStatementProvider(Builder builder) {
        this.tableStatement = builder.tableStatement;
    }

    @Override
    public String getStatement() {
        return "delete from " + tableStatement;
    }

    public static class Builder {
        private String tableStatement;

        public Builder withTable(SqlTable table) {
            this.tableStatement = table.getTotalName();
            return this;
        }

        public DeleteStatementProvider build() {
            return new DeleteStatementProvider(this);
        }
    }

}
