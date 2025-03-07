package com.mobigen.vdap.server.repositories;

import com.mobigen.vdap.server.entity.EntityExtension;
import com.mobigen.vdap.server.entity.EntityExtensionId;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntityExtensionRepository extends JpaRepository<EntityExtension, EntityExtensionId> {
    List<EntityExtension> findByIdAndExtensionStartingWith(String id, String extensionPrefix, Sort sort);

    Optional<EntityExtension> findByIdAndExtension(String id, String extension);
}