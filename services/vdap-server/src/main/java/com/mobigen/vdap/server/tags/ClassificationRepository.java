package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.server.entity.ClassificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassificationRepository extends JpaRepository<ClassificationEntity, String> {
    Optional<ClassificationEntity> findByName(String name);

    void deleteByName(String name);
}
