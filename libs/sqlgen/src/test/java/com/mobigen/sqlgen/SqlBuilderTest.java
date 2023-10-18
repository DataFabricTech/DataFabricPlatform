package com.mobigen.sqlgen;

import com.mobigen.sqlgen.model.JoinHow;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.where.conditions.Equal;
import com.mobigen.sqlgen.where.conditions.GreaterThan;
import com.mobigen.sqlgen.where.conditions.Or;
import org.junit.jupiter.api.Test;

import java.sql.JDBCType;

class SqlBuilderTest {

    @Test
    void select() {
        var table1 = SqlTable.of("test1");
        var table2 = SqlTable.of("test2", "t2");
        var col1 = SqlColumn.of("A a", table1, JDBCType.BIGINT);
        var col2 = SqlColumn.of("B a", table1, JDBCType.BIGINT);
        var col3 = SqlColumn.of("C a", table2, JDBCType.BIGINT);

        var statementProvider = SqlBuilder.select(col1)
                .from(table1)
                .join(table2, Equal.of(col1, col3))
                .join(table2, JoinHow.CROSS)
                .where(Equal.of(col1, "1a"),
                        Equal.of(12, col2),
                        Or.of(GreaterThan.of(12.0, 12), Equal.of(col1, col3))
                )
                .generate();
        System.out.println(statementProvider.getStatement());
    }

    @Test
    void insert() {
        var table1 = SqlTable.of("test1");
        var col1 = SqlColumn.of("A a", table1, JDBCType.BIGINT);
        var col2 = SqlColumn.of("B a", table1, JDBCType.BIGINT);

        var statementProvider = SqlBuilder.insert(table1)
                .columns(col1, col2)
                .values("a", 2)
                .values(1, 2)
                .generate();
        System.out.println(statementProvider.getStatement());
    }

//    @Test
//    void select2() {
//        var d = new Date();
//        var table = org.mybatis.dynamic.sql.SqlTable.of("test");
//        var col1 = org.mybatis.dynamic.sql.SqlColumn.of("A", table, JDBCType.BIGINT);
//        var sqlModel = org.mybatis.dynamic.sql.SqlBuilder.select(col1)
//                .from(table)
////                .join(table).on(col1, EqualTo(col1, ))
//                .where()
//                .build();
//        var res = sqlModel.render(RenderingStrategies.MYBATIS3);
//        System.out.println(res.getSelectStatement());
//    }
}