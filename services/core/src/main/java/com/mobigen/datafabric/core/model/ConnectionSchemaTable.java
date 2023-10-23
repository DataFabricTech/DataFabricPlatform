package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import lombok.Getter;

import java.sql.JDBCType;

@Getter
public class ConnectionSchemaTable {
    SqlTable table = SqlTable.of("ConnectionSchema");
    SqlColumn adaptorIdCol = SqlColumn.of("adaptor_id", table, JDBCType.VARCHAR); // fk
    SqlColumn keyCol = SqlColumn.of("key", table, JDBCType.VARCHAR);
    SqlColumn typeCol = SqlColumn.of("type", table, JDBCType.VARCHAR);
    SqlColumn defaultCol = SqlColumn.of("default", table, JDBCType.BLOB);
    SqlColumn requiredCol = SqlColumn.of("required", table, JDBCType.BOOLEAN);

}
