package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.repository.jpaRepository.StorageRepository;
import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.Storage;
import dto.enums.StatusType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StorageService extends SyncService<Storage, UUID> {
    private final StorageRepository storageRepository;

    public StorageService(JpaRepository<Storage, UUID> repository, StorageRepository storageRepository) {
        super(repository);
        this.storageRepository = storageRepository;
    }

    public List<Storage> findByName(String name, Pageable pageable) {
        return storageRepository.findByName(name,pageable);
    }
    public List<Storage> findByCreatedBy(UUID createdBy, Pageable pageable) {
        return storageRepository.findByCreatedBy(createdBy, pageable);
    }
    public List<Storage> findByModifiedBy(UUID modifiedBy, Pageable pageable) {
        return storageRepository.findByModifiedBy(modifiedBy, pageable);
    }
    public List<Storage> findByStatus(StatusType statusType, Pageable pageable) {
        return storageRepository.findByStatus(statusType, pageable);
    }
}
