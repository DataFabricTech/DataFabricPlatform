package com.mobigen.datafabric.core.connector;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {


    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        var driver = "org.postgresql.Driver";
        var url = "jdbc:postgresql://localhost:5432/postgres";
        var user = "postgres";
        var password = "test";

        Class.forName(driver);
        var conn = DriverManager.getConnection(url, user, password);
        var stmt = conn.createStatement();
        var sql = "select * from public.test";
        var rs = stmt.executeQuery(sql);
        while (rs.next()) {
            int a = rs.getInt("a");
            int b = rs.getInt("b");
            float c = rs.getInt("c");
            System.out.println(a+","+b+","+c);
        }
        rs.close();
        stmt.close();
        conn.close();

    }
}
