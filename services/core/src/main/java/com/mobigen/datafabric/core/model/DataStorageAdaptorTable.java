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
    SqlColumn id = SqlColumn.of("id", table, JDBCType.VARCHAR);  // pk
    SqlColumn storageTypeName = SqlColumn.of("storage_type_name", table, JDBCType.VARCHAR); // fk
    SqlColumn name = SqlColumn.of("name", table, JDBCType.VARCHAR);
    SqlColumn version = SqlColumn.of("version", table, JDBCType.VARCHAR);
    SqlColumn path = SqlColumn.of("path", table, JDBCType.VARCHAR);
    SqlColumn driver = SqlColumn.of("driver", table, JDBCType.VARCHAR);
    SqlColumn createdBy = SqlColumn.of("created_by", table, JDBCType.VARCHAR);
    SqlColumn createdAt = SqlColumn.of("created_at", table, JDBCType.TIMESTAMP);
    SqlColumn updatedBy = SqlColumn.of("updated_by", table, JDBCType.VARCHAR);
    SqlColumn updatedAt = SqlColumn.of("updated_at", table, JDBCType.TIMESTAMP);
    SqlColumn deletedBy = SqlColumn.of("deleted_by", table, JDBCType.VARCHAR);
    SqlColumn deletedAt = SqlColumn.of("deleted_at", table, JDBCType.TIMESTAMP);

}
