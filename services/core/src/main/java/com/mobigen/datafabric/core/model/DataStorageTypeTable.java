package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import lombok.Getter;

import java.sql.JDBCType;

@Getter
public class DataStorageTypeTable {
    SqlTable table = SqlTable.of("DataStorageType");
    SqlColumn nameCol = SqlColumn.of("name", table, JDBCType.VARCHAR);  // pk
    SqlColumn iconCol = SqlColumn.of("icon", table, JDBCType.BLOB);
}
