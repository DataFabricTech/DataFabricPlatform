package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.generate.InsertStatementProvider;
import com.mobigen.sqlgen.generate.StatementProvider;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InsertMaker implements MakerInterface {

    private final SqlTable table;

    private final List<SqlColumn> insertColumns = new ArrayList<>();
    private final List<List<Object>> insertValues = new ArrayList<>();

    private InsertMaker(Builder builder) {
        table = Objects.requireNonNull(builder.table);
    }

    public InsertMaker columns(SqlColumn... insertColumns) {
        this.insertColumns.addAll(List.of(insertColumns));
        return this;
    }

    private boolean checkValueNumber(Object... insertValues) {
        return insertColumns.size() == insertValues.length;
    }

    public InsertMaker values(Object... insertValues) {
        if (!checkValueNumber(insertValues)) {
            throw new RuntimeException("Difference length between column and value.");
        }
        this.insertValues.add(List.of(insertValues));
        return this;
    }

    @Override
    public StatementProvider generate() {
        if (this.insertValues.isEmpty()) {
            throw new RuntimeException("no values");
        }
        return new InsertStatementProvider.Builder()
                .withTable(table)
                .withColumns(insertColumns)
                .withValues(insertValues)
                .build();
    }

    public static InsertMaker insert(SqlTable table) {
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

        private InsertMaker build() {
            return new InsertMaker(this);
        }
    }
}
