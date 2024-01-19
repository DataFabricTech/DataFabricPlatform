package com.mobigen.datafabric.extraction.Postgresql;

import java.util.HashMap;
import java.util.Map;

import static com.mobigen.datafabric.extraction.model.RdbTabledataFormatEnum.*;

public class PostgresqlTypetoFormat {
    static Map<String, Enum> PostgresTypetoFormat = new HashMap<>() {{
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

    public static String PostgresDataFormatDistinct(String dataType) {
        return PostgresTypetoFormat.getOrDefault(dataType, ERROR).name();
    }
}
