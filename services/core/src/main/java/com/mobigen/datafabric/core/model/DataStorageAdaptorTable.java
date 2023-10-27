package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import lombok.Getter;

import java.sql.JDBCType;

/**
 * 실제 Table 의 구조를 정의하는 모델 클래스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
public class DataStorageAdaptorTable {
    SqlTable table = SqlTable.of("DataStorageAdaptor");
    SqlColumn idCol = SqlColumn.of("id", table, JDBCType.VARCHAR);  // pk
    SqlColumn storageTypeNameCol = SqlColumn.of("storage_type_name", table, JDBCType.VARCHAR); // fk
    SqlColumn nameCol = SqlColumn.of("name", table, JDBCType.VARCHAR);
    SqlColumn versionCol = SqlColumn.of("version", table, JDBCType.VARCHAR);
    SqlColumn pathCol = SqlColumn.of("path", table, JDBCType.VARCHAR);
    SqlColumn driverCol = SqlColumn.of("driver", table, JDBCType.VARCHAR);
    SqlColumn createdByCol = SqlColumn.of("created_by", table, JDBCType.VARCHAR);
    SqlColumn createdAtCol = SqlColumn.of("created_at", table, JDBCType.TIMESTAMP);
    SqlColumn updatedByCol = SqlColumn.of("updated_by", table, JDBCType.VARCHAR);
    SqlColumn updatedAtCol = SqlColumn.of("updated_at", table, JDBCType.TIMESTAMP);
    SqlColumn deletedByCol = SqlColumn.of("deleted_by", table, JDBCType.VARCHAR);
    SqlColumn deletedAtCol = SqlColumn.of("deleted_at", table, JDBCType.TIMESTAMP);

}
