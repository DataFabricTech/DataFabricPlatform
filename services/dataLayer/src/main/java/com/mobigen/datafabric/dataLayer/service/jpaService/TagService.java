package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TagService extends SyncService<Tag, UUID> {
    public TagService(JpaRepository<Tag, UUID> repository) {
        super(repository);
    }
}
