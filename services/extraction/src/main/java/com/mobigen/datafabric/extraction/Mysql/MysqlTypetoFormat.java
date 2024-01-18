package com.mobigen.datafabric.extraction.Mysql;

import java.util.HashMap;
import java.util.Map;

import static com.mobigen.datafabric.extraction.model.RdbTabledataFormatEnum.*;

public class MysqlTypetoFormat {

    static Map<String, Enum> MySQLtypetoFormat = new HashMap<>() {{
        put("BIT", INTEGER);
        put("TINYINT", INTEGER);
        put("SMALLINT", INTEGER);
        put("MEDIUMINT", INTEGER);
        put("INT", INTEGER);
        put("BIGINT", INTEGER);
        put("oid", INTEGER);

        put("FLOAT", REAL);
        put("DOUBLE", REAL);

        put("DECIMAL", NUMERIC);

        put("CHAR", TEXT);
        put("VARCHAR", TEXT);
        put("BINARY", TEXT);
        put("VARBINARY", TEXT);
        put("TEXT", TEXT);
        put("TINYTEXT", TEXT);
        put("MEDIUMTEXT", TEXT);
        put("LONGTEXT", TEXT);
        put("JSON", TEXT);

        put("DATE", DATE);

        put("TIME", TIME);

        put("DATETIME", DATETIME);
        put("TIMESTAMP", DATETIME);

        put("BLOB", BLOB);
        put("TINYBLOB", BLOB);
        put("MEDIUMBLOB", BLOB);
        put("LONGBLOB", BLOB);

        put("GEOMETRY", ETC);
        put("ENUM", ETC);
        put("SET", ETC);
    }};

    public static String MySQLDataFormatDistinct(String dataType) {
        return MySQLtypetoFormat.getOrDefault(dataType, ERROR).name();
    }
}
