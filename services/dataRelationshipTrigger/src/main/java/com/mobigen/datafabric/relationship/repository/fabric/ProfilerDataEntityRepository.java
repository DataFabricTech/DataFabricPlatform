package com.mobigen.datafabric.relationship.repository.fabric;

import com.mobigen.datafabric.relationship.dto.fabric.ProfilerDataEntity;
import com.mobigen.datafabric.relationship.dto.fabric.ProfilerDataEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProfilerDataEntityRepository extends JpaRepository<ProfilerDataEntity, ProfilerDataEntityId> {
    @Query("SELECT p.json FROM ProfilerDataEntity p WHERE p.id.entityFQNHash = :entityFQNHash AND p.id.extension = :extension order by p.id.timestamp desc limit 1")
    Optional<String> findProfilerDataEntity(String entityFQNHash, String extension);

//    Optional<ProfilerDataEntity> findFirstById_EntityFQNHashAndId_ExtensionOrderById(String entityFQNHash, String extension);
}