package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.repository.jpaRepository.StorageAdaptorSchemaRepository;
import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.StorageAdaptorSchema;
import dto.enums.AdaptorType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StorageAdaptorSchemaService extends SyncService<StorageAdaptorSchema, UUID> {
    private final StorageAdaptorSchemaRepository storageAdaptorSchemaRepository;

    public StorageAdaptorSchemaService(JpaRepository<StorageAdaptorSchema, UUID> repository, StorageAdaptorSchemaRepository storageAdaptorSchemaRepository) {
        super(repository);
        this.storageAdaptorSchemaRepository = storageAdaptorSchemaRepository;
    }

    public List<StorageAdaptorSchema> findByName(String name, Pageable pageable) {
        return storageAdaptorSchemaRepository.findByName(name, pageable);
    }
    public List<StorageAdaptorSchema> findByAdaptorType(AdaptorType adaptorType, Pageable pageable){
        return storageAdaptorSchemaRepository.findByAdaptorType(adaptorType, pageable);
    }
    public List<StorageAdaptorSchema> findByEnable(boolean enable, Pageable pageable){
        return storageAdaptorSchemaRepository.findByEnable(enable, pageable);
    }

}
