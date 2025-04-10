package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.common.utils.CommonUtil;
import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.entity.data.Glossary;
import com.mobigen.vdap.schema.entity.data.GlossaryTerm;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.TagLabel;
import com.mobigen.vdap.schema.type.TagLabel.TagSource;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.ClassificationEntity;
import com.mobigen.vdap.server.entity.TagEntity;
import com.mobigen.vdap.server.entity.TagUsageEntity;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.mobigen.vdap.server.util.EntityUtil.compareTagLabel;

@Slf4j
@Service
public class TagLabelUtil {
    private final ClassificationRepository classificationRepository;
    private final TagRepository tagRepository;

    public TagLabelUtil(
            ClassificationRepository classificationRepository,
            TagRepository tagRepository) {
        this.tagRepository = tagRepository;
        this.classificationRepository = classificationRepository;
    }
//    private final GlossaryRepository glossaryRepository;
//    private final GlossaryTermRepository glossaryTermRepository;

    public Classification getClassification(UUID id) {
        Optional<ClassificationEntity> entity = classificationRepository.findById(id.toString());
        return entity.map(classificationEntity -> JsonUtils.readValue(classificationEntity.getJson(), Classification.class)).orElse(null);
    }

    public Tag getTag(UUID id) {
        TagEntity entity = tagRepository.findById(id.toString()).orElse(null);
        if (entity == null) {
            return null;
        }
        Tag tag = JsonUtils.readValue(entity.getJson(), Tag.class);
        tag.setClassification(getReference(UUID.fromString(entity.getClassificationId()), Entity.CLASSIFICATION));
        return tag;
    }

    public Glossary getGlossary(UUID id) {
//        return glossaryRepository.getEntityById(id, NON_DELETED);
        return null;
    }

    public GlossaryTerm getGlossaryTerm(UUID id) {
//        return glossaryTermRepository.getEntityById(id, NON_DELETED);
        return null;
    }

    public EntityReference getReference(UUID id, String type) {
        return switch (type) {
            case Entity.CLASSIFICATION -> {
                Classification res = getClassification(id);
                yield new EntityReference()
                        .withId(res.getId())
                        .withType(Entity.CLASSIFICATION)
                        .withName(res.getName())
                        .withDisplayName(res.getDisplayName())
                        .withDescription(res.getDescription());
            }
            case Entity.TAG -> {
                Tag tag = getTag(id);
                yield new EntityReference()
                        .withId(tag.getId())
                        .withType(Entity.TAG)
                        .withName(tag.getName())
                        .withDisplayName(tag.getDisplayName())
                        .withDescription(tag.getDescription());
            }
            default -> throw new CustomException("[Classification/Tag] GetReference : Not Supported Type", type);
        };
    }

    public List<TagLabel> getTagLabels(List<TagUsageEntity> usages) {
        List<TagLabel> tagLabels = new ArrayList<>();
        for (TagUsageEntity tagUsage : usages) {
            TagLabel tagLabel = new TagLabel()
                    .withId(UUID.fromString(tagUsage.getTagId()))
                    .withSource(tagUsage.getSource() == 0 ? TagSource.CLASSIFICATION :
                            tagUsage.getSource() == 1 ? TagSource.GLOSSARY : TagSource.GLOSSARY_TERM);
            tagLabels.add(tagLabel);
        }
        tagLabels.forEach(this::applyTagCommonFields);
        tagLabels.sort(compareTagLabel);
        return tagLabels;
    }

    public void applyTagCommonFields(TagLabel label) {
        switch (label.getSource()) {
            case CLASSIFICATION -> {
                Tag tag = getTag(label.getId());
                label.setParentId(tag.getClassification().getId());
                label.setName(tag.getName());
                label.setDisplayName(tag.getDisplayName());
                label.setDescription(tag.getDescription());
            }
            case GLOSSARY, GLOSSARY_TERM -> {
                GlossaryTerm glossaryTerm = getGlossaryTerm(label.getId());
                label.setParentId(glossaryTerm.getGlossary().getId());
                label.setName(glossaryTerm.getName());
                label.setDisplayName(glossaryTerm.getDisplayName());
                label.setDescription(glossaryTerm.getDescription());
            }
            default -> throw new IllegalArgumentException("Invalid source type " + label.getSource());
        }
    }

    /**
     * Returns true if the parent of the tag label is mutually exclusive
     */
    public boolean mutuallyExclusive(TagLabel label) {
        if (label.getSource() == TagSource.CLASSIFICATION) {
            return getClassification(label.getParentId()).getMutuallyExclusive();
        } else if (label.getSource() == TagSource.GLOSSARY) {
            return getGlossary(label.getParentId()).getMutuallyExclusive();
        } else if (label.getSource() == TagSource.GLOSSARY_TERM) {
            return getGlossaryTerm(label.getParentId()).getMutuallyExclusive();
        } else {
            throw new IllegalArgumentException("Invalid source type " + label.getSource());
        }
    }

    public List<TagLabel> getUniqueTags(List<TagLabel> tags) {
        Set<TagLabel> uniqueTags = new TreeSet<>(compareTagLabel);
        uniqueTags.addAll(tags);
        return uniqueTags.stream().toList();
    }

    public void checkMutuallyExclusive(List<TagLabel> tagLabels) {
        // 동일 영역(depth) 내 에서 서로 배타적인 경우 - 하나의 부모 아래 같은 이름의 태그/단어는 허용하지 않음
        // 배타적인 태그가 설정될 수 없도록 확인.
        Map<String, TagLabel> map = new HashMap<>();
        for (TagLabel tagLabel : CommonUtil.listOrEmpty(tagLabels)) {
            // 두 태그가 동일한 부모를 가지고 있고 그 부모가 상호 배타적인 경우 오류를 발생시킵니다.
            String parentId = tagLabel.getParentId().toString();
            TagLabel stored = map.put(parentId, tagLabel);
            if (stored != null && mutuallyExclusive(tagLabel)) {
                throw new IllegalArgumentException(String.format(
                        "Tag labels %s and %s are mutually exclusive and can't be assigned together",
                        tagLabel.getId(), stored.getName()));
            }
        }
    }

    public void checkDisabledTags(List<TagLabel> tagLabels) {
        for (TagLabel tagLabel : CommonUtil.listOrEmpty(tagLabels)) {
            if (tagLabel.getSource().equals(TagSource.CLASSIFICATION)) {
                Tag tag = getTag(tagLabel.getId());
                if (tag.getDisabled()) {
                    throw new IllegalArgumentException(String.format(
                            "Tag label [%s]/[%s] is disabled and can't be assigned to a data asset.", tag.getId(), tag.getName()));
                }
            } else if (tagLabel.getSource().equals(TagSource.GLOSSARY) || tagLabel.getSource().equals(TagSource.GLOSSARY_TERM)) {
                GlossaryTerm glossaryTerm = getGlossaryTerm(tagLabel.getId());
                if (glossaryTerm.getDisabled()) {
                    throw new IllegalArgumentException(String.format(
                            "Tag label [%s]/[%s] is disabled and can't be assigned to a data asset.", glossaryTerm.getId(), glossaryTerm.getName()));
                }
            }
        }
    }

    public void checkMutuallyExclusiveForParentAndSubField(
            String assetFqn,
            String assetFqnHash,
            Map<String, List<TagLabel>> allAssetTags,
            List<TagLabel> glossaryTags,
            boolean validateSubFields) {
//        boolean failed = false;
//        StringBuilder errorMessage = new StringBuilder();
//
//        Map<String, List<TagLabel>> filteredTags =
//                allAssetTags.entrySet().stream()
//                        .filter(entry -> entry.getKey().startsWith(assetFqnHash))
//                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//        // Check Parent Tags
//        List<TagLabel> parentTags = filteredTags.remove(assetFqnHash);
//
//        if (parentTags != null) {
//            List<TagLabel> tempList = new ArrayList<>(addDerivedTags(parentTags));
//            tempList.addAll(glossaryTags);
//            try {
//                checkMutuallyExclusive(getUniqueTags(tempList));
//            } catch (IllegalArgumentException ex) {
//                failed = true;
//                tempList.removeAll(glossaryTags);
//                errorMessage.append(
//                        String.format(
//                                "Asset %s has a tag %s which is mutually exclusive with the one of the glossary tags %s. %n",
//                                assetFqn,
//                                converTagLabelArrayToString(tempList),
//                                converTagLabelArrayToString(glossaryTags)));
//            }
//        }
//
//        if (validateSubFields) {
//            // Check SubFields Tags
//            Set<TagLabel> subFieldTags =
//                    filteredTags.values().stream().flatMap(List::stream).collect(Collectors.toSet());
//            List<TagLabel> tempList = new ArrayList<>(addDerivedTags(subFieldTags.stream().toList()));
//            tempList.addAll(glossaryTags);
//            try {
//                checkMutuallyExclusive(getUniqueTags(tempList));
//            } catch (IllegalArgumentException ex) {
//                failed = true;
//                errorMessage.append(
//                        String.format(
//                                "Asset %s has a Subfield Column/Schema/Field containing tags %s which is mutually exclusive with the one of the glossary tags %s",
//                                assetFqn,
//                                converTagLabelArrayToString(tempList),
//                                converTagLabelArrayToString(glossaryTags)));
//            }
//        }
//
//        // Throw Exception if failed
//        if (failed) {
//            throw new IllegalArgumentException(errorMessage.toString());
//        }
    }

    public String converTagLabelArrayToString(List<TagLabel> tags) {
        return String.format(
                "[%s]", tags.stream().map(tag -> tag.getId().toString()).collect(Collectors.joining(", ")));
    }

}
