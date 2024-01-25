package com.mobigen.datafabric.dataLayer.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.Getter;

@Getter
public class JpaConfig {
    private final EntityManager em;
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("fabric");

    public JpaConfig() {
        this.em = emf.createEntityManager();
    }

}
