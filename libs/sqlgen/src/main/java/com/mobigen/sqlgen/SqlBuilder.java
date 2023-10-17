package com.mobigen.sqlgen;

import com.mobigen.sqlgen.maker.QueryMaker;
import com.mobigen.sqlgen.model.SqlColumn;

public class SqlBuilder {

    public static QueryMaker.Builder select(SqlColumn... columns) {
        return QueryMaker.select(columns);
    }

}
