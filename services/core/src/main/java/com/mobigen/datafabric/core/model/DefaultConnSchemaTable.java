package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;

import java.sql.JDBCType;

public class DefaultConnSchemaTable {
    SqlTable table = SqlTable.of("DefaultConnSchema");
    SqlColumn storageTypeNameCol = SqlColumn.of("datastorage_type_name", table, JDBCType.VARCHAR); // fk
    SqlColumn keyCol = SqlColumn.of("key", table, JDBCType.VARCHAR);
    SqlColumn typeCol = SqlColumn.of("type", table, JDBCType.VARCHAR);
    SqlColumn defaultCol = SqlColumn.of("default", table, JDBCType.BLOB);
    SqlColumn requiredCol = SqlColumn.of("required", table, JDBCType.BOOLEAN);

}
