package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.generate.DeleteStatementProvider;
import com.mobigen.sqlgen.generate.StatementProvider;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.where.Condition;

import java.util.List;
import java.util.Objects;

/**
 * Delete 절을 만들기 위한 값을 빌드하는 클래스
 * statement provider 를 생성 한다.
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class DeleteMaker implements WhereUsable {

    private final SqlTable table;

    private DeleteMaker(Builder builder) {
        table = Objects.requireNonNull(builder.table);
    }

    @Override
    public StatementProvider generate() {
        return new DeleteStatementProvider.Builder()
                .withTable(table)
                .build();
    }

    @Override
    public WhereMaker where(Condition... conditions) {
        return new WhereMaker.Builder()
                .withMaker(this)
                .withConditions(List.of(conditions))
                .build();
    }

    public static DeleteMaker delete(SqlTable table) {
        return new Builder()
                .withTable(table)
                .build();
    }

    private static class Builder {
        private SqlTable table;

        private Builder withTable(SqlTable table) {
            this.table = table;
            return this;
        }

        private DeleteMaker build() {
            return new DeleteMaker(this);
        }
    }
}
