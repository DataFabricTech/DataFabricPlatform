package com.mobigen.sqlgen;

import com.mobigen.sqlgen.maker.where.Equal;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.where.WhereModel;

import java.sql.JDBCType;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class SqlBuilderTest {

    @Test
    void select() {
        var table = SqlTable.of("test");
        var col1 = SqlColumn.of("A a", table, JDBCType.BIGINT);
        var col2 = SqlColumn.of("B a", table, JDBCType.BIGINT);
        var col3 = SqlColumn.of("C a", table, JDBCType.BIGINT);
        var statementProvider = SqlBuilder.select(col1)
                .from(table)
                .where(Equal.of(col1, "1a"),
                        Equal.of(col2, 12),
                        Equal.of(12.0, 12),
                        Equal.of(col1, col3)
                )
                .generate();
        System.out.println(statementProvider.getStatement());
    }

    @Test
    void select2() {
        var d = new Date();
        var table = org.mybatis.dynamic.sql.SqlTable.of("test");
        var col1 = org.mybatis.dynamic.sql.SqlColumn.of("A", table, JDBCType.BIGINT);
        var sqlModel = org.mybatis.dynamic.sql.SqlBuilder.select(col1)
                .from(table)
                .build();
        var res = sqlModel.render(RenderingStrategies.MYBATIS3);
        System.out.println(res.getSelectStatement());
    }
}