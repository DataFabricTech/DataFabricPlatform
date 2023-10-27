package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import lombok.Getter;

import java.sql.JDBCType;

@Getter
public class UrlFormatTable {
    SqlTable table = SqlTable.of("UrlFormat");
    SqlColumn adaptorId = SqlColumn.of("adaptor_id", table, JDBCType.VARCHAR); // fk
    SqlColumn format = SqlColumn.of("format", table, JDBCType.VARCHAR);
}
