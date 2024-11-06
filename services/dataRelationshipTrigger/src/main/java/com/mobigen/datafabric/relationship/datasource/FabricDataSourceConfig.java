package com.mobigen.datafabric.relationship.datasource;

import com.mobigen.datafabric.relationship.configurations.Configurations;
import com.mobigen.datafabric.relationship.configurations.fabric.FabricStorage;
import com.mobigen.datafabric.relationship.configurations.fabric.StorageType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
        basePackages = "com.mobigen.datafabric.relationship.repository.fabric",
        entityManagerFactoryRef = "fabricEntityManagerFactory",
        transactionManagerRef = "fabricTransactionManager"
)
@EnableTransactionManagement
public class FabricDataSourceConfig {

    private final Configurations configurations;

    public FabricDataSourceConfig(Configurations configurations) {
        this.configurations = configurations;
    }

    @Bean(name = "fabricEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(getJpaProperties());
        em.setPackagesToScan(new String[]{"com.mobigen.datafabric.relationship.dto.fabric"});
        em.setDataSource(fabricDataSource(configurations.getFabric().getStorage()));

        return em;
    }

    private Properties getJpaProperties() {
        Properties properties = new Properties();
        if (configurations.getFabric().getStorage().getSchema().getValue().equals("mysql")) {
            // mysql dialect
            properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect"); // 원하는 Dialect 지정
        } else {
            // postgresql dialect
            properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        }
        // 테이블 자동 생성
        properties.put("hibernate.ddl.auto", "none");
        return properties;
    }

    @Bean(name = "fabricDataSource")
    public DataSource fabricDataSource(FabricStorage fabricStorage) {
        String url = String.format("jdbc:%s://%s:%d/%s?characterEncoding=UTF-8",
                fabricStorage.getSchema().getValue().toLowerCase(),
                fabricStorage.getHost(),
                fabricStorage.getPort(),
                fabricStorage.getDatabase());

        var driver = "";
        if(fabricStorage.getSchema() == StorageType.MYSQL) {
            driver = "com.mysql.cj.jdbc.Driver";
        } else {
            driver = "org.postgresql.Driver";
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(fabricStorage.getUser());
        config.setPassword(fabricStorage.getPassword());
        config.setDriverClassName(driver);

        // HikariCP 커넥션 풀 설정 (옵션)
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(600000);

        return new HikariDataSource(config);
    }

    @Bean(name = "fabricTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Autowired @Qualifier("fabricEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }
}
