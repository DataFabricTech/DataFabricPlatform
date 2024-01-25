package com.mobigen.datafabric.dataLayer.repository;

import dto.Tag;
import jakarta.persistence.EntityManager;

public class TagRepository extends JPARepository<Tag> {
    public TagRepository(EntityManager em) {
        super(Tag.class, em);
    }
}
