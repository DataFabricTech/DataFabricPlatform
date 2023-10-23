package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import lombok.Getter;

import java.sql.JDBCType;

@Getter
public class AdaptorUsableAuthTable {
    SqlTable table = SqlTable.of("AdaptorUsableAuth");
    SqlColumn adaptorIdCol = SqlColumn.of("adaptor_id", table, JDBCType.VARCHAR); // fk
    SqlColumn authTypeCol = SqlColumn.of("auth_type", table, JDBCType.VARCHAR);
}
