package com.mobigen.datafabric.relationship.datasource;

import com.mobigen.datafabric.relationship.configurations.Configurations;
import com.mobigen.datafabric.relationship.configurations.dolphin.DolphinStorage;
import com.mobigen.datafabric.relationship.configurations.dolphin.StorageType;
import com.mobigen.datafabric.relationship.configurations.fabric.FabricStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Slf4j
@Configuration
@EnableJpaRepositories(
        basePackages = "com.mobigen.datafabric.relationship.repository.dolphin",
        entityManagerFactoryRef = "dolphinEntityManagerFactory",
        transactionManagerRef = "dolphinTransactionManager"
)
@EnableTransactionManagement
public class DolphinDataSourceConfig {

    private final Configurations configurations;

    public DolphinDataSourceConfig(Configurations configurations) {
        this.configurations = configurations;
    }

    @Bean(name = "dolphinEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(getJpaProperties(configurations.getDolphin().getStorage()));
        em.setPackagesToScan(new String[]{"com.mobigen.datafabric.relationship.dto.dolphin"});
        em.setDataSource(dolphinDataSource(configurations.getDolphin().getStorage()));

        return em;
    }

    private Properties getJpaProperties(DolphinStorage dolphinStorage) {
        Properties properties = new Properties();
        if (dolphinStorage.getSchema() == StorageType.MYSQL) {
            // mysql dialect
            properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect"); // 원하는 Dialect 지정
        } else {
            // postgresql dialect
            properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        }
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", false);
        // 테이블 자동 생성
        properties.put("hibernate.ddl.auto", "none");
        return properties;
    }

    @Bean(name = "dolphinDataSource")
    public DataSource dolphinDataSource(DolphinStorage dolphinStorage) {
        String url = String.format("jdbc:%s://%s:%d/%s?characterEncoding=UTF-8",
                dolphinStorage.getSchema().getValue().toLowerCase(),
                dolphinStorage.getHost(),
                dolphinStorage.getPort(),
                dolphinStorage.getDatabase());

        String driver = dolphinStorage.getSchema().getValue().equals("mysql") ?
                "com.mysql.cj.jdbc.Driver" : "org.postgresql.Driver";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(dolphinStorage.getUser());
        config.setPassword(dolphinStorage.getPassword());
        config.setDriverClassName(driver);

        // HikariCP 커넥션 풀 설정 (옵션)
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(600000);

        return new HikariDataSource(config);
    }

    @Bean(name = "dolphinTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Autowired @Qualifier("dolphinEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }
}
