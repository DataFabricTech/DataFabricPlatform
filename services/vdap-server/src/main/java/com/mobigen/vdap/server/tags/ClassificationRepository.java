package com.mobigen.vdap.server.tags;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobigen.vdap.server.entity.ClassificationEntity;

@Repository
public interface ClassificationRepository extends JpaRepository<ClassificationEntity, String> {
    Optional<ClassificationEntity> findByName(String name);

    void deleteByName(String name);
}
