package com.mobigen.sqlgen;

import com.mobigen.sqlgen.maker.QueryMaker;

public class SqlBuilder {

    public static QueryMaker select(SqlColumn... columns) {
        return QueryMaker.select(columns);
    }

}
