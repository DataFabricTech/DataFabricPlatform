package com.mobigen.dolphin.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        basePackages = "com.mobigen.dolphin.repository.openmetadata",
        entityManagerFactoryRef = "openMetadataEntityManager",
        transactionManagerRef = "openMetadataTransactionManager"
)
public class OpenMetadataConfiguration {
    @Bean(value = "openmetadataHibernateProperties")
    @ConfigurationProperties("dolphin.openmetadata.hibernate.property")
    public Map<String, Object> openmetadataHibernateProperties() {
        return new HashMap<>();
    }

    @Bean(value = "openmetadataHikariConfig")
    @ConfigurationProperties("dolphin.openmetadata.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean(name = "openmetadataDataSource")
    public DataSource openMetadataDataSource() {
        return new HikariDataSource(hikariConfig());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean openMetadataEntityManager() {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(openMetadataDataSource());
        em.setPackagesToScan("com.mobigen.dolphin.entity.openmetadata");
        var vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        //Hibernate 설정
        var properties = openmetadataHibernateProperties();
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    public PlatformTransactionManager openMetadataTransactionManager() {
        var transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(openMetadataEntityManager().getObject());
        return transactionManager;
    }
}
