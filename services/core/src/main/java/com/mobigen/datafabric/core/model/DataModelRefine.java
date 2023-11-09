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
public class DataModelRefine {
    public static SqlTable table = SqlTable.of( "data_refine" );
    public static SqlColumn id = SqlColumn.of( "id", table, JDBCType.VARCHAR );
    public static SqlColumn sql = SqlColumn.of( "sql", table, JDBCType.VARCHAR );
    public static SqlColumn json = SqlColumn.of( "json", table, JDBCType.VARCHAR );
}
