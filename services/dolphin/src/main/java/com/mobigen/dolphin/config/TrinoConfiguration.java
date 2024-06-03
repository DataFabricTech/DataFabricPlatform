package com.mobigen.dolphin.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.mobigen.dolphin.repository.trino",
        entityManagerFactoryRef = "trinoEntityManager",
        transactionManagerRef = "trinoTransactionManager"
)
public class TrinoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(TrinoConfiguration.class);

    @Bean(value = "trinoHibernateProperties")
    @ConfigurationProperties("dolphin.trino.hibernate.property")
    public Map<String, Object> trinoHibernateProperties() {
        return new HashMap<>();
    }

    @Bean(value = "trinoHikariConfig")
    @ConfigurationProperties("dolphin.trino.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean(name = "trinoDataSource")
    public DataSource trinoDataSource() {
        return new HikariDataSource(hikariConfig());
    }

    @Bean(name = "trinoJdbcTemplate")
    public JdbcTemplate trinoJdbcTemplate() {
        return new JdbcTemplate(trinoDataSource());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean trinoEntityManager() {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(trinoDataSource());
        em.setPackagesToScan("com.mobigen.dolphin.entity.trino");
        var vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        //Hibernate 설정
        var properties = trinoHibernateProperties();
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    public PlatformTransactionManager trinoTransactionManager() {
        var transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(trinoEntityManager().getObject());
        return transactionManager;
    }

    public boolean isDBConnected() {
        boolean isConnected = false;
        try (var connection = trinoDataSource().getConnection();
             var statement = connection.prepareStatement("select 1")) {
            log.info("DB is connected : {}", statement.execute());
            isConnected = true;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return isConnected;
    }
}
