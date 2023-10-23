package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import lombok.Getter;

import java.sql.JDBCType;

@Getter
public class DefaultConnSchemaTable {
    SqlTable table = SqlTable.of("DefaultConnSchema");
    SqlColumn storageTypeNameCol = SqlColumn.of("storage_type_name", table, JDBCType.VARCHAR); // fk
    SqlColumn keyCol = SqlColumn.of("key", table, JDBCType.VARCHAR);
    SqlColumn typeCol = SqlColumn.of("type", table, JDBCType.VARCHAR);
    SqlColumn defaultCol = SqlColumn.of("default", table, JDBCType.BLOB);
    SqlColumn requiredCol = SqlColumn.of("required", table, JDBCType.BOOLEAN);

}
