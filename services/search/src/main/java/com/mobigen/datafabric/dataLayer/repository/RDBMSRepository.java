package com.mobigen.datafabric.dataLayer.repository;

import com.google.protobuf.ByteString;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.libs.grpc.Time;
import com.mobigen.libs.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonpUtils;

import java.sql.*;
import java.text.SimpleDateFormat;

@Slf4j
public class RDBMSRepository {
    private final DBConfig dbConfig;

    public RDBMSRepository(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public void executeUpdate(String sql) throws SQLException, ClassNotFoundException {
        log.info("[executeUpdate] start");
        // TODO insert/update의 경우 update_at 같은 것을 넣어줘야 한다.
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            stmt.executeUpdate(sql); // todo 보안 취약점 발생
            conn.commit();
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void executeBatchUpdate(String[] sqls) throws SQLException, ClassNotFoundException {
        log.info("[executeBatchUpdate] start");
        try (
                var conn = getConnection();
                var stmt = conn.createStatement();
        ) {
            for (var sql : sqls)
                stmt.addBatch(sql);
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException | ClassNotFoundException e) {
            log.error("[executeBatchUpdate] cause : {}, message : {}", e.getCause(), e.getMessage());
            throw e;
        }
    }

    public Table executeQuery(String sql) throws SQLException, ClassNotFoundException {
        log.info("[executeQuery] start");
        Column column;
        Row row;
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
                column = Column.newBuilder().setColumnName(rs.getMetaData().getColumnName(i))
                        .setType(setColumnType(rs.getMetaData().getColumnType(i)))
                        .build();
                tableBuilder.addColumn(column);
            }

            while (rs.next()) {
                Rows.Builder rowsBuilder = Rows.newBuilder();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row = switch (rs.getMetaData().getColumnType(i)) {
                        case 4, 5 -> // INTEGER, SMALLINT
                                Row.newBuilder()
                                        .setInt32Value(rs.getInt(i))
                                        .setColumnName(rs.getMetaData().getColumnName(i))
                                        .build();
                        case 6, 7 -> // FLOAT, REAL
                                Row.newBuilder()
                                        .setFloatValue(rs.getFloat(i))
                                        .setColumnName(rs.getMetaData().getColumnName(i))
                                        .build();
                        case 8, 3, 2 -> // DOUBLE, NUMERIC, DECIMAL
                                Row.newBuilder()
                                        .setDoubleValue(rs.getDouble(i))
                                        .setColumnName(rs.getMetaData().getColumnName(i))
                                        .build();
                        case -2, -3, 2004 -> // BINARY, VARBINARY, BLOB
                                Row.newBuilder()
                                        .setBytesValue(ByteString.copyFrom(rs.getBytes(i)))
                                        .setColumnName(rs.getMetaData().getColumnName(i))
                                        .build();
                        case 91 -> { // DATE
                            time = Time.newBuilder()
                                    .setTime(rs.getDate(i).toString())
                                    .setFormat("yyyy-mm-dd")
                                    .build();
                            yield Row.newBuilder()
                                    .setTimeValue(time)
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                        }
                        case 92 -> { // Time
                            time = Time.newBuilder()
                                    .setTime(rs.getTime(i).toString())
                                    .setFormat("HH:MM:ss")
                                    .build();
                            yield Row.newBuilder()
                                    .setTimeValue(time)
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                        }
                        case 93 -> { // TIMESTAMP
                            if (rs.getTimestamp(i) != null) {
                                time = Time.newBuilder()
                                        .setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").format(rs.getTimestamp(i)))
                                        .setFormat("yyyy-MM-dd HH:mm:ss.sss")
                                        .build();
                                yield Row.newBuilder()
                                        .setTimeValue(time)
                                        .setColumnName(rs.getMetaData().getColumnName(i))
                                        .build();
                            } else {
                                yield Row.newBuilder().build();
                            }
                        }
                        case 16, -6 -> // BOOLEAN, TINYINT
                                Row.newBuilder()
                                        .setBoolValue(rs.getBoolean(i))
                                        .setColumnName(rs.getMetaData().getColumnName(i))
                                        .build();
                        case -5 -> // BIGINT
                                Row.newBuilder()
                                        .setInt64Value(rs.getLong(i))
                                        .setColumnName(rs.getMetaData().getColumnName(i))
                                        .build();
                        default -> // Others
                                rs.getString(i) != null ?
                                        Row.newBuilder()
                                                .setStringValue(rs.getString(i))
                                                .setColumnName(rs.getMetaData().getColumnName(i))
                                                .build()
                                        :
                                        Row.newBuilder()
                                                .setStringValue("")
                                                .setColumnName(rs.getMetaData().getColumnName(i))
                                                .build();

                    };
                    rowsBuilder.addRow(row);
                }
                tableBuilder.addRows(rowsBuilder.build());
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw e;
        }
        return tableBuilder.build();
    }

    public String setColumnType(int columTypeEnum) {
        return switch (columTypeEnum) {
            case 4, 5 -> // INTEGER, SMALLINT
                    "int32";
            case 6, 7 -> // FLOAT, REAL
                    "float";
            case 8, 3, 2 -> // DOUBLE, NUMERIC, DECIMAL
                    "double";
            case -2, -3, 2004 -> // BINARY, VARBINARY, BLOB
                    "bytes";
            case 91 -> "date";
            case 92 -> "time";
            case 93 -> "timestamp";
            case 16, -6 -> // BOOLEAN, TINYINT
                    "boolean";
            case -5 -> // BIGINT
                    "int64";
            default -> // Others4
                    "string";
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
