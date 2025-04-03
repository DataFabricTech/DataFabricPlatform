package com.mobigen.vdap.server.extensions;

import com.mobigen.vdap.server.entity.EntityExtension;
import com.mobigen.vdap.server.entity.EntityExtensionId;
import com.mobigen.vdap.server.entity.RelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtensionRepository extends JpaRepository<EntityExtension, EntityExtensionId>,
        JpaSpecificationExecutor<EntityExtension> {
//    List<EntityExtension> findByIdAndExtensionStartingWith(String id, String extensionPrefix, Sort sort);
//
//    Optional<EntityExtension> findByIdAndExtension(String id, String extension);
//
//    void deleteByIdAndExtensionStartingWith(String id, String extensionPrefix);
}