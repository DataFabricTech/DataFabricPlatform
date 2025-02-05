package com.mobigen.datafabric.relationship.repository.fabric;

import com.mobigen.datafabric.relationship.dto.fabric.GlossaryTermEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlossaryTermEntityRepository extends JpaRepository<GlossaryTermEntity, String> {
    GlossaryTermEntity findGlossaryTermEntityByFqnHash(String fqnHash);
}