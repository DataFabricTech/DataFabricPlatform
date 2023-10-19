package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.generate.InsertUpdateStatementProvider;
import com.mobigen.sqlgen.generate.StatementProvider;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.where.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpdateMaker implements MakerInterface {

    private final SqlTable table;

    private final List<SqlColumn> updateColumns = new ArrayList<>();
    private final List<List<Object>> updateValues = new ArrayList<>();

    private UpdateMaker(Builder builder) {
        table = Objects.requireNonNull(builder.table);
    }

    public UpdateMaker columns(SqlColumn... insertColumns) {
        this.updateColumns.addAll(List.of(insertColumns));
        return this;
    }

    private boolean checkValueNumber(Object... insertValues) {
        return updateColumns.size() == insertValues.length;
    }

    public UpdateMaker values(Object... insertValues) {
        if (!checkValueNumber(insertValues)) {
            throw new RuntimeException("Difference length between column and value.");
        }
        this.updateValues.add(List.of(insertValues));
        return this;
    }

    @Override
    public StatementProvider generate() {
        if (this.updateValues.isEmpty()) {
            throw new RuntimeException("no values");
        }
        return new InsertUpdateStatementProvider.Builder(false)
                .withTable(table)
                .withColumns(updateColumns)
                .withValues(updateValues)
                .build();
    }

    public WhereMaker where(Condition... conditions) {
        return new WhereMaker.Builder()
                .withMaker(this)
                .withConditions(List.of(conditions))
                .build();
    }

    public static UpdateMaker update(SqlTable table) {
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

        private UpdateMaker build() {
            return new UpdateMaker(this);
        }
    }
}
