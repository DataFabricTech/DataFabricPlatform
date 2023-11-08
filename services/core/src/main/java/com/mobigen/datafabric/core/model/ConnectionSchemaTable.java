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
public class ConnectionSchemaTable {
    public static SqlTable table = SqlTable.of("ConnectionSchema");
    public static SqlColumn adaptorId = SqlColumn.of("adaptor_id", table, JDBCType.VARCHAR); // fk
    public static SqlColumn key = SqlColumn.of("key", table, JDBCType.VARCHAR);
    public static SqlColumn type = SqlColumn.of("type", table, JDBCType.VARCHAR);
    public static SqlColumn defaultCol = SqlColumn.of("default", table, JDBCType.BLOB);
    public static SqlColumn required = SqlColumn.of("required", table, JDBCType.BOOLEAN);
    public static SqlColumn basic = SqlColumn.of("basic", table, JDBCType.BOOLEAN);
}
