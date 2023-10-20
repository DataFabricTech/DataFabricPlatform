package com.mobigen.datafabric.dataLayer.repository;

import com.google.protobuf.ByteString;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.libs.grpc.Time;
import com.mobigen.libs.grpc.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.text.SimpleDateFormat;

@Slf4j
public class RDBMSRepository {
    private final DBConfig dbConfig;

    public RDBMSRepository(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }


    public QueryResponseMessage insertQuery(String query) {
        try (var rs = executeQuery(query)) {
            return QueryResponseMessage.newBuilder().setSuccess(true).build();
        } catch (SQLException e) {
            // todo 여기에 실패 이유에 대한 log 필요 아마 e를 이용하면 될 듯 하다.
            return QueryResponseMessage.newBuilder().setSuccess(false).build();
        }
    }

    public QueryResponseMessage execute(String query) {
        try (var rs = executeQuery(query)) {
            return QueryResponseMessage.newBuilder().setSuccess(true).build();
        } catch (SQLException e) {
            log.error("Query Execute Fail");
            return QueryResponseMessage.newBuilder().setSuccess(false).build();
        }
    }

    public QueryResponseMessage deleteQuery(String query) {
        try (var rs = executeQuery(query)) {
            return QueryResponseMessage.newBuilder().setSuccess(true).build();
        } catch (SQLException e) {
            // todo 여기에 실패 이유에 대한 log 필요 아마 e를 이용하면 될 듯 하다.
            return QueryResponseMessage.newBuilder().setSuccess(false).build();
        }
    }

    public QueryResponseMessage updateQuery(String query) {
        try (ResultSet rs = executeQuery(query)) {

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public QueryResponseMessage selectQuery(String query) {
        Column column;
        Row row;
        Time time;
        Rows.Builder rowsBuilder = Rows.newBuilder();
        QueryResponseMessage.Builder queryResponseMessageBuilder = QueryResponseMessage.newBuilder();

        try (ResultSet rs = executeQuery(query)) {
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                column = Column.newBuilder().setColumnName(rs.getMetaData().getColumnName(i))
                        .setType(setColumnType(rs.getMetaData().getColumnType(i)))
                        .build();
                queryResponseMessageBuilder.addColumn(column);
            }

            /**
             * reference
             * https://documentation.softwareag.com/webmethods/adapters_estandards/Adapters/JDBC/JDBC_10-3/10-3_Adapter_for_JDBC_webhelp/index.html#page/jdbc-webhelp/co-jdbc_data_type_to_java_data_type.html
             */
            while (rs.next()) {
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    switch (rs.getMetaData().getColumnType(i)) {
                        case 4, 5: // INTEGER, SMALLINT
                            row = Row.newBuilder()
                                    .setInt32Value(rs.getInt(i))
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                            break;
                        case 6, 7: // FLOAT, REAL
                            row = Row.newBuilder()
                                    .setFloatValue(rs.getFloat(i))
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                            break;
                        case 8, 3, 2: // DOUBLE, NUMERIC, DECIMAL
                            row = Row.newBuilder()
                                    .setDoubleValue(rs.getDouble(i))
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                            break;
                        case -2, -3, 2004: // BINARY, VARBINARY, BLOB
                            row = Row.newBuilder()
                                    .setBytesValue(ByteString.copyFrom(rs.getBytes(i)))
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                            break;
                        case 91: // DATE
                            time = Time.newBuilder()
                                    .setTime(rs.getDate(i).toString())
                                    .setFormat("yyyy-mm-dd")
                                    .build();

                            row = Row.newBuilder()
                                    .setTimeValue(time)
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                            break;
                        case 92: // Time
                            time = Time.newBuilder()
                                    .setTime(rs.getTime(i).toString())
                                    .setFormat("HH:MM:ss")
                                    .build();
                            row = Row.newBuilder()
                                    .setTimeValue(time)
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                            break;
                        case 93: // TIMESTAMP
                            time = Time.newBuilder()
                                    .setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").format(rs.getTimestamp(i)))
                                    .setFormat("yyyy-MM-dd HH:mm:ss.sss")
                                    .build();

                            row = Row.newBuilder()
                                    .setTimeValue(time)
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                            break;
                        case 16, -6: // BOOLEAN, TINYINT
                            row = Row.newBuilder()
                                    .setBoolValue(rs.getBoolean(i))
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                            break;
                        case -5: // BIGINT
                            row = Row.newBuilder()
                                    .setInt64Value(rs.getLong(i))
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                            break;
                        default: // Others
                            row = Row.newBuilder()
                                    .setStringValue(rs.getString(i))
                                    .setColumnName(rs.getMetaData().getColumnName(i))
                                    .build();
                            break;
                    }
                    rowsBuilder.addRow(row);
                }
                queryResponseMessageBuilder.addRows(rowsBuilder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryResponseMessageBuilder.build();
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

    private ResultSet executeQuery(String query) throws SQLException {
        String url = dbConfig.getUrl();
        String user = dbConfig.getUsername();
        String pw = dbConfig.getPassword();

        try (Connection conn = DriverManager.getConnection(url, user, pw);
             Statement stmt = conn.createStatement();
        ) {
            return stmt.executeQuery(query);
        }
    }
}
