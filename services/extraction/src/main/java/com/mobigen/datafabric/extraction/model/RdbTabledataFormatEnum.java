package com.mobigen.datafabric.extraction.model;

import java.sql.JDBCType;

public enum RdbTabledataFormatEnum {
    INTEGER("INTEGER", JDBCType.INTEGER, JDBCType.SMALLINT, JDBCType.BIGINT),
    REAL("REAL", JDBCType.REAL, JDBCType.DOUBLE),
    NUMERIC("NUMERIC", JDBCType.NUMERIC),
    TEXT("TEXT", JDBCType.CHAR, JDBCType.VARCHAR, JDBCType.OTHER),
    DATE("DATE", JDBCType.DATE),
    TIME("TIME", JDBCType.TIME),
    DATETIME("DATETIME", JDBCType.TIMESTAMP),
    BOOL("BOOLEAN", JDBCType.BIT), //애매
    BLOB("BLOB", JDBCType.BLOB),  //애매
    ETC("ETC", JDBCType.OTHER, JDBCType.REF_CURSOR),
    ERROR("UNDEFINED TYPE", JDBCType.OTHER);

    RdbTabledataFormatEnum(String type, JDBCType jdbcType) {
    }

    RdbTabledataFormatEnum(String type, JDBCType jdbcType1, JDBCType jdbcType2) {
    }

    RdbTabledataFormatEnum(String type, JDBCType jdbcType1, JDBCType jdbcType2, JDBCType jdbcType3) {
    }
}
