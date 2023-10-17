package com.mobigen.sqlgen.old;

import com.mobigen.sqlgen.model.SqlColumn;

import java.util.List;

public class SelectDSL {
    public static <T> QueryExpressionDSL.FromGatherer<T> select(SqlColumn... columns) {
        return new QueryExpressionDSL.FromGatherer.Builder<T>()
                .withColumns(List.of(columns))
                .build();
    }
}
