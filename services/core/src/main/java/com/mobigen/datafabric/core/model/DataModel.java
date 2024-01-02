package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;

import java.sql.JDBCType;

/**
 * 실제 Table 의 구조를 정의하는 모델 클래스
 * <p>
 * Created by jblim.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class DataModel {
    public static SqlTable table = SqlTable.of("data_model");
    public static SqlColumn id = SqlColumn.of("id", table, JDBCType.VARCHAR);
    public static SqlColumn name = SqlColumn.of("name", table, JDBCType.VARCHAR);
    public static SqlColumn description = SqlColumn.of("description", table, JDBCType.VARCHAR);
    public static SqlColumn type = SqlColumn.of("type", table, JDBCType.VARCHAR);
    public static SqlColumn format = SqlColumn.of("format", table, JDBCType.VARCHAR);
    public static SqlColumn status = SqlColumn.of("status", table, JDBCType.VARCHAR);

    public static SqlColumn createdAt = SqlColumn.of("created_at", table, JDBCType.TIMESTAMP);
    public static SqlColumn createdBy = SqlColumn.of("created_by", table, JDBCType.VARCHAR);
    public static SqlColumn lastModifiedAt = SqlColumn.of("last_modified_at", table, JDBCType.TIMESTAMP);
    public static SqlColumn lastModifiedBy = SqlColumn.of("last_modified_by", table, JDBCType.VARCHAR);
}
