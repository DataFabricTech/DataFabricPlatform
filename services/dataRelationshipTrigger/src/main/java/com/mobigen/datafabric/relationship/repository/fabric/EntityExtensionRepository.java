package com.mobigen.datafabric.relationship.repository.fabric;

import com.mobigen.datafabric.relationship.dto.fabric.EntityExtension;
import com.mobigen.datafabric.relationship.dto.fabric.EntityExtensionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntityExtensionRepository extends JpaRepository<EntityExtension, EntityExtensionId> {
    @Query("SELECT e " +
            "FROM EntityExtension e " +
            "WHERE e.id.id = :id " +
            "AND e.id.extension like CONCAT(:extensionPrefix, '.%') " +
            "ORDER BY e.id.extension")
    List<EntityExtension> getExtensions(@Param("id") String id, @Param("extensionPrefix") String extensionPrefix);
}