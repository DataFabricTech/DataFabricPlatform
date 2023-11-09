package com.mobigen.datafabric.core.worker.task;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SQL_Generator {
    public String getDatabaseOwner(String storageType, String databaseName ) throws Exception {
        switch( storageType.toLowerCase() ) {
            case "postgresql":
                return "SELECT pg_catalog.pg_get_userbyid(d.datdba) as \"Owner\"\n" +
                        "FROM pg_catalog.pg_database d WHERE d.datname = '" + databaseName + "'";
            default:
                throw new Exception( "Not Supported Storage Type" );
        }
    }
    public String getTablesInfo( String storageType ) throws Exception {
        switch( storageType.toLowerCase() ) {
            case "postgresql":
                return "SELECT pg.tablename, info.table_type, pg.tableowner FROM pg_catalog.pg_tables as pg " +
                        "INNER JOIN information_schema.tables as info ON pg.tablename = info.table_name " +
                        "WHERE pg.schemaname != 'pg_catalog' AND pg.schemaname != 'information_schema'";
            case "mysql", "mariadb":
                return "select table_name, table_type, table_rows, create_time, table_comment from " +
                        "information_schema.TABLES where TABLE_SCHEMA != 'information_schema'";
            default:
                throw new Exception( "Not Supported Storage Type" );
        }
    }

    public String getTableSize( String storageType, String tableName ) throws Exception {
        return switch( storageType.toLowerCase() ) {
            case "postgresql", "mysql", "mariadb" -> "SELECT COUNT(*) FROM " + tableName;
            default -> throw new Exception( "Not Supported Storage Type" );
        };
    }
    public String getTableComment( String storageType, String tableName ) throws Exception {
        return switch( storageType.toLowerCase() ) {
            case "postgresql" -> "SELECT obj_description(pc.oid) FROM pg_catalog.pg_tables as pg " +
                    "INNER JOIN pg_class as pc ON pg.tablename = pc.relname WHERE pg.tablename = '" + tableName + "'";
            case "mariadb", "mysql" -> "SELECT table_comment FROM information_schema.tables " +
                    "WHERE table_schema != 'information_schema' AND table_name = '" + tableName + "'";
            default -> throw new Exception( "Not Supported Storage Type" );
        };
    }

    public String getColumnCount( String storageType ) throws Exception {
        switch( storageType.toLowerCase() ) {
            case "postgresql":
                return "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ?";
            case "mysql":
                return "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ?";
            case "mariadb":
                return "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ?";
            default:
                throw new Exception( "Not Supported Storage Type" );
        }
    }

    public String getTableStructure( String storageType, String tableName ) throws Exception {
        switch( storageType.toLowerCase() ) {
            case "postgresql":
                return
                        "SELECT " +
                            "info.ordinal_position, " +
                            "info.column_name, " +
                            "info.data_type, " +
                            "info.numeric_precision, " +
                            "info.character_octet_length, " +
                            "info.column_default, " +
                            "(select col_description(att.attrelid, att.attnum)) as comment " +
                        "FROM pg_catalog.pg_attribute as att " +
                                "INNER JOIN pg_catalog.pg_class as pc ON att.attrelid = pc.oid " +
                                "INNER JOIN information_schema.columns as info " +
                                    "ON info.table_name = pc.relname and info.column_name = att.attname " +
                        "WHERE info.table_name='" + tableName + "'";
            case "mysql", "mariadb":
                return "SELECT ORDINAL_POSITION, COLUMN_NAME, DATA_TYPE, NUMERIC_PRECISION, " +
                        "CHARACTER_MAXIMUM_LENGTH, COLUMN_DEFAULT, COLUMN_COMMENT " +
                        "FROM information_schema.COLUMNS WHERE TABLE_NAME = '" + tableName + "'";
            default:
                throw new Exception( "Not Supported Storage Type" );
        }
    }
    /* Table Size
    SELECT table_schema as `DB`, table_name AS `Table`,
      ROUND(((data_length + index_length) / 1024 / 1024), 2) `Size (MB)`
      FROM information_schema.TABLES
      ORDER BY (data_length + index_length) DESC;
    * */
}
