package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TagService extends JpaService<Tag, UUID> {
    public TagService(JpaRepository<Tag, UUID> repository) {
        super(repository);
    }
}
