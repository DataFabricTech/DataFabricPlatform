package com.mobigen.datafabric.core.old.connector;

import lombok.Setter;

import java.sql.*;

@Setter
public class RDBConnector implements ConnectorInterface {
    Connection conn;
    Statement statement;
    ResultSet resultSet;

    private String url;
    private String driver;
    private String user;
    private String password;
    private String database;
    public RDBConnector(String urlFormat, ResultSet info) throws SQLException {
        url = urlFormat; // format host, port ,,,,
        var host = info.getString("host");
        var port = info.getInt("port");
        user = info.getString("user");
        password = info.getString("password");
        var db = info.getString("db");
//                var options =
//        for (var key : schema.keySet()) {
//            var value = info.getString(key);
//        }


    }

    @Override
    public void connect() throws SQLException {
        conn = DriverManager.getConnection(url, user, password);
    }

    @Override
    public void execute(String sql) {

    }

    @Override
    public void close() throws Exception {
        try{
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
