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
public class DataStorageTagTable {
    public static SqlTable table = SqlTable.of("DataStorageTag");
    public static SqlColumn datastorageId = SqlColumn.of("datastorage_id", table, JDBCType.VARCHAR);
    public static SqlColumn userId = SqlColumn.of("user_id", table, JDBCType.VARCHAR);
    public static SqlColumn tag = SqlColumn.of("tag", table, JDBCType.VARCHAR);
}
