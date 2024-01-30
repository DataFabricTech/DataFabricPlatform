package dto.enums;

public enum ColumnType {
    BIT, TINYINT, SMALLINT, MEDIUMINT, INT, BIGINT, oid, FLOAT, DOUBLE, DECIMAL, CHAR,
    VARCHAR, BINARY, VARBINARY, TEXT,TINYTEXT, MEDIUMTEXT, LONGTEXT, JSON, DATE, TIME, DATETIME,
    TIMESTAMP, BLOB, TINYBLOB, MEDIUMBLOB, LONGBLOB, GEOMETRY, ENUM, SET, int4, int8, money,
    float4, float8, numeric, charType, varchar, bpchar, text, varbit, name, date, time, timetz,
    timestamp, timestamptz, bool, bit, bytea, refcursor, json, point, box,
    INTEGER, NEWDATE, NULL, OLDDECIMAL, STRING, VARSTRING, YEAR
} //charType > char 으로 작동하는지 추후 확인 필요함
