package com.mobigen.datafabric.relationship.repository.fabric;

import com.mobigen.datafabric.relationship.dto.fabric.TagUsage;
import com.mobigen.datafabric.relationship.dto.fabric.TagUsageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagUsageRepository extends JpaRepository<TagUsage, TagUsageId> {
    @Query("SELECT tagUsage " +
            "FROM TagUsage tagUsage " +
            "WHERE tagUsage.id.targetFQNHash = :targetFQNHash order by tagUsage.tagFQN")
    List<TagUsage> findTagUsageById_TargetFQNHash(@Param("targetFQNHash") String targetFQNHash);
}