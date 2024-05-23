package com.mobigen.dolphin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

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
    @Bean
    @ConfigurationProperties(prefix = "spring.trino-datasource")
    public DataSource trinoDataSource() {
        return DataSourceBuilder.create()
                .build();
    }

    @Bean(name = "trinoJdbcTemplate")
    public JdbcTemplate trinoJdbcTemplate() {
        return new JdbcTemplate(trinoDataSource());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean trinoEntityManager() {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(trinoDataSource());
        em.setPackagesToScan(new String[]{"com.mobigen.dolphin.model"});
        return em;
    }

    @Bean
    public PlatformTransactionManager trinoTransactionManager() {
        var transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(trinoEntityManager().getObject());
        return transactionManager;
    }
}
