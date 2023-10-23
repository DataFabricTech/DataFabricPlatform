package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import lombok.Getter;

import java.sql.JDBCType;

@Getter
public class ConnInfoTable {
    SqlTable table = SqlTable.of("ConnInfo");
    SqlColumn storageTypeNameCol = SqlColumn.of("datastorage_id", table, JDBCType.VARCHAR); // fk
    SqlColumn keyCol = SqlColumn.of("key", table, JDBCType.VARCHAR);
    SqlColumn valueCol = SqlColumn.of("value", table, JDBCType.BLOB);
}
