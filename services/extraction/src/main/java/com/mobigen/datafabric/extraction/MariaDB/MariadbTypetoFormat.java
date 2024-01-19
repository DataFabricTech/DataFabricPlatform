package com.mobigen.datafabric.extraction.Mariadb;

import java.util.HashMap;
import java.util.Map;

import static com.mobigen.datafabric.extraction.model.RdbTabledataFormatEnum.*;

public class MariadbTypetoFormat {

    static Map<String, Enum> MariaTypetoFormat = new HashMap<>() {{
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

    public static String MariaDBDataFormatDistinct(String dataType) {
        return MariaTypetoFormat.getOrDefault(dataType, ERROR).name();
    }
}
