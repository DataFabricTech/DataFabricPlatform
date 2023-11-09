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
public class DataModelSchema {
    public static SqlTable table = SqlTable.of( "data_model_schema" );
    public static SqlColumn id = SqlColumn.of( "id", table, JDBCType.VARCHAR );
    public static SqlColumn ordinalPosition = SqlColumn.of( "ordinal_position", table, JDBCType.INTEGER );
    public static SqlColumn columnName = SqlColumn.of( "column_name", table, JDBCType.VARCHAR );
    public static SqlColumn dataType = SqlColumn.of( "data_type", table, JDBCType.VARCHAR );
    public static SqlColumn length = SqlColumn.of( "length", table, JDBCType.BIGINT);
    public static SqlColumn defaultValue  = SqlColumn.of( "default", table, JDBCType.VARCHAR);
    public static SqlColumn description = SqlColumn.of( "description", table, JDBCType.VARCHAR );
    public static SqlColumn userComment = SqlColumn.of( "user_comment", table, JDBCType.VARCHAR );
}
