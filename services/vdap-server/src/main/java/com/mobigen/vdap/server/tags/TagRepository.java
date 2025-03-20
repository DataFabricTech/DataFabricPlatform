package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.server.entity.TagEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, String> {
    Page<TagEntity> findAllByClassificationId(String classificationId, Pageable pageable);

    Integer countByClassificationId(String classificationId);
}
