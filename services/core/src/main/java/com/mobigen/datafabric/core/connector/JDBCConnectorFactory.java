package com.mobigen.datafabric.core.connector;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;

public class JDBCConnectorFactory {
    public ConnectorInterface getConnector(String type, Map<String, String> schema, ResultSet info) throws SQLException {
        ConnectorInterface connector = null;
        switch (type) {
            case "postgresql":
            case "mysql":
                System.out.println("rdb");
                connector = new RDBConnector("", info);
                break;
            case "mongodb":
                System.out.println("nosql");
                break;
            case "hdfs":
                System.out.println("file db");
                break;
            default:
                System.out.println("not support type");
        }
        return connector;
    }

    Type getType(String type) {
        if (type.equals("TEXT")) {
            return String.class;
        } else {
            return Object.class;
        }
    }

    <T> T getValue(String key, ResultSet info, Function<String, T> function) throws SQLException {
        return function.apply(info.getString(key));
    }

}
