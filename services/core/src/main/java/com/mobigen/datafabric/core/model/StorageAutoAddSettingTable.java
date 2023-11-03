package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.maker.SelectMaker;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import lombok.Getter;

import java.sql.JDBCType;

import static com.mobigen.sqlgen.maker.SelectMaker.select;

/**
 * 실제 Table 의 구조를 정의하는 모델 클래스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
public class StorageAutoAddSettingTable {
    SqlTable table = SqlTable.of("StorageAutoAddSetting");
    SqlColumn datastorageId = SqlColumn.of("datastorage_id", table, JDBCType.VARCHAR);
    SqlColumn regex = SqlColumn.of("regex", table, JDBCType.VARCHAR);
    SqlColumn dataType = SqlColumn.of("data_type", table, JDBCType.VARCHAR);
    SqlColumn dataFormat = SqlColumn.of("data_format", table, JDBCType.VARCHAR);
    SqlColumn minSize = SqlColumn.of("min_size", table, JDBCType.INTEGER);
    SqlColumn maxSize = SqlColumn.of("max_size", table, JDBCType.INTEGER);
    SqlColumn startDate = SqlColumn.of("start_date", table, JDBCType.VARCHAR);
    SqlColumn endDate = SqlColumn.of("end_date", table, JDBCType.VARCHAR);

    public SelectMaker selectAll() {
        return select().from(table);
    }
}
