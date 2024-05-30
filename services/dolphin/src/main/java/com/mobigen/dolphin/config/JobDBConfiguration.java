package com.mobigen.dolphin.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
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
        basePackages = "com.mobigen.dolphin.repository.local",
        entityManagerFactoryRef = "jobEntityManager",
        transactionManagerRef = "jobTransactionManager"
)
public class JobDBConfiguration {
    @Bean(value = "jobHibernateProperties")
    @ConfigurationProperties("dolphin.job.hibernate.property")
    public Map<String, Object> jobHibernateProperties() {
        return new HashMap<>();
    }

    @Bean(value = "jobHikariConfig")
    @ConfigurationProperties("dolphin.job.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Primary
    @Bean(value = "jobDataSource")
    public DataSource dataSource() {
        return new HikariDataSource(hikariConfig());
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean jobEntityManager() {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.mobigen.dolphin.entity.local");
        var vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        var properties = jobHibernateProperties();
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Primary
    @Bean
    public PlatformTransactionManager jobTransactionManager() {
        var transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(jobEntityManager().getObject());
        return transactionManager;
    }
}
