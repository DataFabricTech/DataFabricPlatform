//package com.mobigen.datafabric.core.old.models;
//
//import com.mobigen.datafabric.core.old.models.entities.DataStorageEntity;
//import com.mobigen.datafabric.core.old.models.entities.DataStorageTypeEntity;
//import org.hibernate.cfg.AvailableSettings;
//import org.hibernate.cfg.Configuration;
//
//import java.util.Properties;
//
//public class DBConnection {
//    String className;
//
//    public DBConnection() {
//
//    }
//
//    public static void main(String[] args) {
//        //# PostgreSQL
//        //jakarta.persistence.jdbc.url=jdbc:postgresql://localhost:5432/postgres
//        //# Credentials
//        //jakarta.persistence.jdbc.user=postgres
//        //jakarta.persistence.jdbc.password=test
//        //
//        //# SQL statement logging
//        //hibernate.show_sql=true
//        //hibernate.format_sql=true
//        //hibernate.highlight_sql=true
//
//        var properties = new Properties();
//        properties.setProperty("javax.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/postgres");
//        properties.setProperty("javax.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/postgres");
//        properties.setProperty("javax.persistence.jdbc.user", "postgres");
//        properties.setProperty("javax.persistence.jdbc.password", "test");
//        properties.setProperty("hibernate.show_sql", "true");
//        properties.setProperty("hibernate.format_sql", "true");
//        properties.setProperty("hibernate.highlight_sql", "true");
//        var cfg = new Configuration()
//                .addAnnotatedClass(DataStorageEntity.class)
//                .addAnnotatedClass(DataStorageTypeEntity.class)
//                .setProperty(AvailableSettings.JAKARTA_JDBC_URL, "jdbc:postgresql://localhost:5432/postgres")
//                .setProperty(AvailableSettings.JAKARTA_JDBC_USER, "postgres")
//                .setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, "test")
//                .setProperty("jakarta.persistence.schema-generation.database.action", "drop-and-create")
//                .setProperty("hibernate.show_sql", "true")
//                .setProperty("hibernate.format_sql", "true")
//                .setProperty("hibernate.highlight_sql", "true");
//
////        var sb = new StandardServiceRegistryBuilder();
////        sb.applySettings(cfg.getProperties());
//        try (var sessionFactory = cfg.buildSessionFactory()) {
//
//        }
//
//
//    }
//}
