package com.mobigen.datafabric.dataLayer.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.Getter;

@Getter
public class JpaAgent {
    private final EntityManager em;
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("fabric");

    public JpaAgent() {
        this.em = emf.createEntityManager();
    }

}
