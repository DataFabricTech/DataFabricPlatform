package com.mobigen.datafabric.dataLayer.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

public class JpaConfig {
    private static volatile EntityManager em;

    private JpaConfig() {
    }

    public static EntityManager getEntityManager() {
        if (em == null || !em.isOpen()) {
            synchronized (EntityManager.class) {
                var emf = Persistence.createEntityManagerFactory("fabric");
                em = emf.createEntityManager();
                em.getTransaction().begin();
            }
        }
        return em;
    }
}
