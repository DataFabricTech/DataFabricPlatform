package com.mobigen.vdap.server.relationship;

import com.mobigen.vdap.server.entity.TagUsageEntity;
import com.mobigen.vdap.server.entity.TagUsageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagUsageRepository extends JpaRepository<TagUsageEntity, TagUsageId>, JpaSpecificationExecutor<TagUsageEntity> {

    Integer countBySourceAndSourceId(int source, String sourceId);

    Integer countBySourceAndTagId(int source, String tagId);

    void deleteByTagId(String tagId);

    List<TagUsageEntity> findByTargetTypeAndTargetId(String targetType, String targetId);
}