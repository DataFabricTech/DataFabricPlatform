package com.mobigen.sqlgen;

import com.mobigen.sqlgen.maker.InsertMaker;
import com.mobigen.sqlgen.maker.SelectMaker;
import com.mobigen.sqlgen.maker.UpdateMaker;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;

public class SqlBuilder {

    public static SelectMaker.FromGatherer select(SqlColumn... columns) {
        return SelectMaker.select(columns);
    }

    public static InsertMaker insert(SqlTable table) {
        return InsertMaker.insert(table);
    }

    public static UpdateMaker update(SqlTable table) {
        return UpdateMaker.update(table);
    }

}