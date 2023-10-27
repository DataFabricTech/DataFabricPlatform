package com.mobigen.sqlgen;

import com.mobigen.sqlgen.maker.DeleteMaker;
import com.mobigen.sqlgen.maker.InsertMaker;
import com.mobigen.sqlgen.maker.SelectMaker;
import com.mobigen.sqlgen.maker.UpdateMaker;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;

/**
 * SQL 을 생성하기 위하여 시작 이 되는 static 함수 정의
 * sqlgen 의 시작점
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class SqlBuilder {

    public static SelectMaker.FromGatherer select(SqlColumn... columns) {
        return SelectMaker.select(columns);
    }

    public static InsertMaker insert(SqlTable table) {
        return InsertMaker.insert(table);
    }

    public static UpdateMaker update(SqlTable table) {
        return UpdateMaker.update(table);
    }

    public static DeleteMaker drop(SqlTable table) {
        return DeleteMaker.delete(table);
    }

}
