package com.mobigen.datafabric.dataLayer.repository;

import dto.Tag;

public class TagRepository extends JPARepository<Tag> {
    public TagRepository() {
        super(Tag.class);
    }
}
