package com.mobigen.datafabric.extraction.extraction;

import java.util.HashMap;
import java.util.Map;

import static com.mobigen.datafabric.extraction.model.RDBTabledataFormat.*;

public class RDBdataTypetoFormat {
    Map<String, Enum> typetoFormat = new HashMap<>(){{
            put("int4", INTEGER);
            put("int8", INTEGER);
            put("oid", INTEGER);

            put("money", REAL);
            put("float4", REAL);
            put("float8", REAL);

            put("numeric", NUMERIC);

            put("char", TEXT);
            put("varchar", TEXT);
            put("bpchar", TEXT);
            put("text", TEXT);
            put("varbit", TEXT);
            put("name", TEXT);

            put("date", DATE);

            put("time", TIME);
            put("timetz", TIME);

            put("timestamp", DATETIME);
            put("timestamptz", DATETIME);

            put("bool", BOOL);
            put("bit", BOOL);
            put("bytea", BLOB);

            put("refcursor", ETC);
            put("json", ETC);
            put("point", ETC);
            put("box", ETC);
        }};

    Map<String, Enum> MySQLtypetoFormat = new HashMap<>(){{
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

    Map<String, Enum> MariatypetoFormat = new HashMap<>(){{
            put("TINYINT", INTEGER);
            put("SMALLINT", INTEGER);
            put("INTEGER", INTEGER);
            put("BIGINT", INTEGER);
            put("MEDIUMINT", INTEGER);

            put("FLOAT", REAL);
            put("DOUBLE", REAL);

            put("DECIMAL", NUMERIC);
            put("OLDDECIMAL", NUMERIC);

            put("BIT", TEXT);
            put("VARCHAR", TEXT);
            put("JSON", TEXT);
            put("ENUM", TEXT);
            put("SET", TEXT);
            put("VARSTRING", TEXT);
            put("STRING", TEXT);

            put("DATE", DATE);
            put("YEAR", DATE);

            put("TIME", TIME);

            put("DATETIME", DATETIME);
            put("TIMESTAMP", DATETIME);
            put("NEWDATE", DATETIME);

            put("TINYBLOB", BLOB);
            put("MEDIUMBLOB", BLOB);
            put("LONGBLOB", BLOB);
            put("BLOB", BLOB);

            put("NULL", ETC);
            put("GEOMETRY", ETC);
        }};

    public String dataFormatDistinct(String dataType) {
        return typetoFormat.getOrDefault(dataType, ERROR).name();
    }
}
