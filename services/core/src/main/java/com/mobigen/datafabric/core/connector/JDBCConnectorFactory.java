package com.mobigen.datafabric.core.connector;

public class JDBCConnectorFactory {
    public ConnectorInterface getConnector(String type) {
        ConnectorInterface connector = null;
        switch (type) {
            case "postgresql":
            case "mysql":
                System.out.println("rdb");
                connector = new RDBConnector();
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
}
