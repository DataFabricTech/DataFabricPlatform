package com.mobigen.vdap.server.relationship;

import com.mobigen.vdap.common.utils.CommonUtil;
import com.mobigen.vdap.schema.type.TagLabel;
import com.mobigen.vdap.server.entity.TagUsageEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TagUsageService {
    private final TagUsageRepository tagUsageRepository;

    public TagUsageService(TagUsageRepository tagUsageRepository) {
        this.tagUsageRepository = tagUsageRepository;
    }

    private Specification<TagUsageEntity> withDynamicConditions(
            TagLabel.TagSource source, String sourceID, String tagID, String targetType, String targetId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (source != null) {
                predicates.add(criteriaBuilder.equal(root.get("source"), source.ordinal()));
            }
            if (sourceID != null) {
                predicates.add(criteriaBuilder.equal(root.get("sourceId"), sourceID));
            }
            if (tagID != null) {
                predicates.add(criteriaBuilder.equal(root.get("tagId"), tagID));
            }
            if (targetType != null) {
                predicates.add(criteriaBuilder.equal(root.get("targetType"), targetType));
            }
            if (targetId != null) {
                predicates.add(criteriaBuilder.equal(root.get("targetId"), targetId));
            }
            // 조건이 없으면 항상 true를 반환하여 모든 데이터 조회
            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public void applyTags(List<TagLabel> tags, String targetType, String targetId) {
        for (TagLabel tagLabel : CommonUtil.listOrEmpty(tags)) {
            TagUsageEntity entity = new TagUsageEntity();
            entity.setSource(tagLabel.getSource().ordinal());
            entity.setSourceId(tagLabel.getParentId().toString());
            entity.setTagId(tagLabel.getId().toString());
            entity.setTargetType(targetType);
            entity.setTargetId(targetId);
            log.info("[TagUsageService] Insert TagUsage Source[{}/{}] Tag[{}] Target(Dest)[{}/{}]",
                    tagLabel.getSource(), tagLabel.getParentId(), tagLabel.getId(), targetType, targetId);
            tagUsageRepository.save(entity);
        }
    }

    public List<TagUsageEntity> getTagUsages(
            TagLabel.TagSource source, String sourceID, String tagID, String targetType, String targetId) {
        log.info("[TagUsageService] GetTagUsage Source[{}/{}] Tag[{}] Target(Dest)[{}/{}]",
                source, sourceID, tagID, targetType, targetId);
        return tagUsageRepository.findAll(withDynamicConditions(source, sourceID, tagID, targetType, targetId));
    }

    public Integer getCount(TagLabel.TagSource source, String sourceId, String tagId, String targetType, String targetId) {
        log.info("[TagUsageService] GetCount TagUsage Source[{}/{}] Tag[{}] Target(Dest)[{}/{}]",
                source, sourceId, tagId, targetType, targetId);
        return Math.toIntExact(tagUsageRepository.count(withDynamicConditions(source, sourceId, tagId, targetType, targetId)));
    }

    public void delete(TagLabel.TagSource source, String sourceId, String tagId, String targetType, String targetId) {
        log.info("[TagUsageService] Delete TagUsage Source[{}/{}] Tag[{}] Target(Dest)[{}/{}]",
                source, sourceId, tagId, targetType, targetId);
        tagUsageRepository.delete(withDynamicConditions(source, sourceId, tagId, targetType, targetId));
    }
}
