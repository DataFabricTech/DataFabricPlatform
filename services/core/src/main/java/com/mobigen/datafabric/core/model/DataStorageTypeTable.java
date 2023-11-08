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
public class DataStorageTypeTable {
    public static SqlTable table = SqlTable.of("DataStorageType");
    public static SqlColumn nameCol = SqlColumn.of("name", table, JDBCType.VARCHAR);  // pk
    public static SqlColumn iconCol = SqlColumn.of("icon", table, JDBCType.BLOB);
}
