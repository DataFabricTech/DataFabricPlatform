package com.mobigen.datafabric.core.collector;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class DataCollectorFactory {
    public static DataCollector getCollector(String database) {
        switch (database.toLowerCase()) {
            case "postgresql", "mysql", "mariadb" -> {
                return new JdbcCollector();
            }
            default -> {
                return null;
            }
        }
    }
}
