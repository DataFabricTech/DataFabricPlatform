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
public class DefaultConnSchemaTable {
    public static SqlTable table = SqlTable.of("DefaultConnSchema");
    public static SqlColumn storageTypeNameCol = SqlColumn.of("storage_type_name", table, JDBCType.VARCHAR); // fk
    public static SqlColumn keyCol = SqlColumn.of("key", table, JDBCType.VARCHAR);
    public static SqlColumn typeCol = SqlColumn.of("type", table, JDBCType.VARCHAR);
    public static SqlColumn defaultCol = SqlColumn.of("default", table, JDBCType.BLOB);
    public static SqlColumn requiredCol = SqlColumn.of("required", table, JDBCType.BOOLEAN);

}
