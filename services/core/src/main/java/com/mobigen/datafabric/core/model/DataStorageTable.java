package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import lombok.Getter;

import java.sql.JDBCType;

@Getter
public class DataStorageTable {
    SqlTable table = SqlTable.of("DataStorage");
    SqlColumn idCol = SqlColumn.of("id", table, JDBCType.VARCHAR);
    SqlColumn adaptorCol = SqlColumn.of("adaptor", table, JDBCType.VARCHAR);
    SqlColumn nameCol = SqlColumn.of("name", table, JDBCType.VARCHAR);
    SqlColumn userDescCol = SqlColumn.of("user_desc", table, JDBCType.VARCHAR);
    SqlColumn totalDataCol = SqlColumn.of("total_data", table, JDBCType.INTEGER);
    SqlColumn regiDataCol = SqlColumn.of("regi_data", table, JDBCType.INTEGER);
    SqlColumn createdByCol = SqlColumn.of("created_by", table, JDBCType.VARCHAR);
    SqlColumn createdAtCol = SqlColumn.of("created_at", table, JDBCType.TIMESTAMP);
    SqlColumn updatedByCol = SqlColumn.of("updated_by", table, JDBCType.VARCHAR);
    SqlColumn updatedAtCol = SqlColumn.of("updated_at", table, JDBCType.TIMESTAMP);
    SqlColumn deletedByCol = SqlColumn.of("deleted_by", table, JDBCType.VARCHAR);
    SqlColumn deletedAtCol = SqlColumn.of("deleted_at", table, JDBCType.TIMESTAMP);
    SqlColumn statusCol = SqlColumn.of("status", table, JDBCType.INTEGER);
    SqlColumn lastConnectionCheckedAtCol = SqlColumn.of("last_connection_checked_at", table, JDBCType.TIMESTAMP);
    SqlColumn lastSyncAtCol = SqlColumn.of("last_sync_at", table, JDBCType.TIMESTAMP);

}
