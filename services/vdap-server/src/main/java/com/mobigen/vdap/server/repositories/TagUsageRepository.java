package com.mobigen.vdap.server.repositories;

import com.mobigen.vdap.server.entity.TagUsageEntity;
import com.mobigen.vdap.server.entity.TagUsageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagUsageRepository extends JpaRepository<TagUsageEntity, TagUsageId> {

    Integer countBySourceAndSourceId(int source, String sourceId);

    Integer countBySourceAndTagId(int source, String tagId);

    void deleteByTagId(String tagId);
}