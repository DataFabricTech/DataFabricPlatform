package com.mobigen.datafabric.dataLayer.repository;

import com.google.protobuf.ByteString;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.share.protobuf.DataLayer.*;
import com.mobigen.datafabric.share.protobuf.Utilities;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class DataLayerRepository {
    private final DBConfig dbConfig;

    public DataLayerRepository(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public int executeUpdate(String sql) throws SQLException, ClassNotFoundException {
        log.info("[executeUpdate] start");
        // TODO insert/update의 경우 update_at 같은 것을 넣어줘야 한다.
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            var rs = stmt.executeUpdate(sql); // todo 보안 취약점 발생

            conn.commit();
            return rs;
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage());
            throw e;
        }


    }

    public int[] executeBatchUpdate(String[] sqls) throws SQLException, ClassNotFoundException {
        log.info("[executeBatchUpdate] start");

        try (
                var conn = getConnection();
                var stmt = conn.createStatement();
        ) {
            for (var sql : sqls)
                stmt.addBatch(sql);
            var rs = stmt.executeBatch();
            conn.commit();
            return rs;
        } catch (SQLException | ClassNotFoundException e) {
            log.error("[executeBatchUpdate] cause : {}, message : {}", e.getCause(), e.getMessage());
            throw e;
        }
    }

    public Table executeQuery(String sql) throws SQLException, ClassNotFoundException {
        log.info("[executeQuery] start");
        Column column;
        Cell cell;
        Time time;
        Table.Builder tableBuilder = Table.newBuilder();
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql); // todo 보안 취약점 발생으로 인한 처리 필요
        ) {
            /*
              reference
              https://documentation.softwareag.com/webmethods/adapters_estandards/Adapters/JDBC/JDBC_10-3/10-3_Adapter_for_JDBC_webhelp/index.html#page/jdbc-webhelp/co-jdbc_data_type_to_java_data_type.html
             */
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                column = Column.newBuilder()
                        .setColumnName(rs.getMetaData().getColumnName(i))
                        .setType(setColumnType(rs.getMetaData().getColumnType(i)))
                        .build();
                tableBuilder.addColumns(column);
            }

            while (rs.next()) {
                Row.Builder rowsBuilder = Row.newBuilder();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    cell = switch (rs.getMetaData().getColumnType(i)) {
                        case Types.INTEGER, Types.SMALLINT ->  // INTEGER, SMALLINT
                                Cell.newBuilder()
                                        .setInt32Value(rs.getInt(i))
                                        .setColumnIndex(i - 1)
                                        .build();
                        case Types.FLOAT, Types.REAL -> // FLOAT, REAL
                                Cell.newBuilder()
                                        .setFloatValue(rs.getFloat(i))
                                        .setColumnIndex(i - 1)
                                        .build();
                        case Types.DOUBLE, Types.NUMERIC, Types.DECIMAL  -> // DOUBLE, NUMERIC, DECIMAL
                                Cell.newBuilder()
                                        .setDoubleValue(rs.getDouble(i))
                                        .setColumnIndex(i - 1)
                                        .build();
                        case Types.BINARY, Types.VARBINARY, Types.BLOB -> // BINARY, VARBINARY, BLOB
                                Cell.newBuilder()
                                        .setBytesValue(ByteString.copyFrom(rs.getBytes(i)))
                                        .setColumnIndex(i - 1)
                                        .build();
//                        case 91 -> { // DATE todo
//                            time = Time.newBuilder()
//                                    .setTime(rs.getDate(i).toString())
//                                    .setFormat("yyyy-mm-dd")
//                                    .build();
//                            yield Cell.newBuilder()
//                                    .setTimeValue(time)
//                                    .setColumnIndex(i)
//                                    .build();
//                        }
//                        case 92 -> { // Time
//                            time = Utilities.DateTime.newBuilder()
//                                    .setTime(rs.getTime(i).toString())
//                                    .setFormat("HH:MM:ss")
//                                    .build();
//                            yield Cell.newBuilder()
//                                    .setTimeValue(time)
//                                    .setColumnIndex(i)
//                                    .build();
//                        }
//                        case 93 -> { // TIMESTAMP
//                            if (rs.getTimestamp(i) != null) {
//                                time = Time.newBuilder()
//                                        .setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").format(rs.getTimestamp(i)))
//                                        .setFormat("yyyy-MM-dd HH:mm:ss.sss")
//                                        .build();
//                                yield Cell.newBuilder()
//                                        .setTimeValue(time)
//                                        .setColumnIndex(i)
//                                        .build();
//                            } else {
//                                yield Cell.newBuilder().build();
//                            }
//                        }
                        case Types.BOOLEAN, Types.TINYINT ->  // BOOLEAN, TINYINT
                                Cell.newBuilder()
                                        .setBoolValue(rs.getBoolean(i))
                                        .setColumnIndex(i - 1)
                                        .build();
                        case Types.BIGINT -> // BIGINT
                                Cell.newBuilder()
                                        .setInt64Value(rs.getLong(i))
                                        .setColumnIndex(i - 1)
                                        .build();
                        default -> // Others
                                rs.getString(i) != null ?
                                        Cell.newBuilder()
                                                .setStringValue(rs.getString(i))
                                                .setColumnIndex(i - 1)
                                                .build()
                                        :
                                        Cell.newBuilder()
                                                .setStringValue("")
                                                .setColumnIndex(i - 1)
                                                .build();

                    };
                    rowsBuilder.addCell(cell);
                }
                tableBuilder.addRows(rowsBuilder.build());
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw e;
        }
        return tableBuilder.build();
    }

    public Utilities.DataType setColumnType(int columTypeEnum) {
        return switch (columTypeEnum) {
            case Types.INTEGER, Types.SMALLINT -> // INTEGER, SMALLINT
                    Utilities.DataType.INT32;
            case Types.FLOAT, Types.REAL -> // FLOAT, REAL
                    Utilities.DataType.FLOAT;
            case Types.DOUBLE, Types.NUMERIC, Types.DECIMAL  -> // DOUBLE, NUMERIC, DECIMAL
                    Utilities.DataType.DOUBLE;
            case Types.BINARY, Types.VARBINARY, Types.BLOB  -> // BINARY, VARBINARY, BLOB
                    Utilities.DataType.BYTES;
            case 91, 92, 93 -> Utilities.DataType.DATETIME;
            case Types.BOOLEAN, Types.TINYINT -> // BOOLEAN, TINYINT
                    Utilities.DataType.BOOL;
            case Types.BIGINT -> // BIGINT
                    Utilities.DataType.INT64;
            default -> // Others4
                    Utilities.DataType.STRING;
        };
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        String url = dbConfig.getUrl();
        String user = dbConfig.getUsername();
        String pw = dbConfig.getPassword();

        try {
            Class.forName("org.postgresql.Driver");
            var conn = DriverManager.getConnection(url, user, pw);
            conn.setAutoCommit(false);
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            log.error("[getConnection] cause : {}, message : {}", e.getCause(), e.getMessage());
            throw e;
        }
    }
}
