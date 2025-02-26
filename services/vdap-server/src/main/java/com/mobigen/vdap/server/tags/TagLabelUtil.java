/*
 *  Copyright 2021 Collate
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.common.utils.CommonUtil;
import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.entity.data.Glossary;
import com.mobigen.vdap.schema.entity.data.GlossaryTerm;
import com.mobigen.vdap.schema.type.TagLabel;
import com.mobigen.vdap.schema.type.TagLabel.TagSource;
import com.mobigen.vdap.server.entity.ClassificationEntity;
import com.mobigen.vdap.server.entity.TagEntity;
import com.mobigen.vdap.server.util.EntityUtil;
import com.mobigen.vdap.server.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagLabelUtil {
    private final TagRepository tagRepository;
    private final ClassificationRepository classificationRepository;

    public TagLabelUtil(TagRepository tagRepository, ClassificationRepository classificationRepository) {
        this.tagRepository = tagRepository;
        this.classificationRepository = classificationRepository;
    }
//    private final GlossaryRepository glossaryRepository;
//    private final GlossaryTermRepository glossaryTermRepository;
//    private final TagUsageRepository tagUsageRepository;

    public Classification getClassification(UUID id) {
        Optional<ClassificationEntity> entity = classificationRepository.findById(id.toString());
        return entity.map(classificationEntity -> JsonUtils.readValue(classificationEntity.getJson(), Classification.class)).orElse(null);
    }

    public Tag getTag(UUID id) {
        Optional<TagEntity> entity = tagRepository.findById(id.toString());
        return entity.map(tagEntity -> JsonUtils.readValue(tagEntity.getJson(), Tag.class)).orElse(null);
    }

    public Glossary getGlossary(UUID id) {
//        return glossaryRepository.getEntityById(id, NON_DELETED);
        return null;
    }

    public GlossaryTerm getGlossaryTerm(UUID id) {
//        return glossaryTermRepository.getEntityById(id, NON_DELETED);
        return null;
    }

    public void applyTagCommonFields(TagLabel label) {
        if (label.getSource() == TagSource.CLASSIFICATION) {
            Tag tag = getTag(label.getId());
            label.setName(tag.getName());
            label.setDisplayName(tag.getDisplayName());
            label.setDescription(tag.getDescription());
        } else if (label.getSource() == TagSource.GLOSSARY) {
            GlossaryTerm glossaryTerm = getGlossaryTerm(label.getId());
            label.setName(glossaryTerm.getName());
            label.setDisplayName(glossaryTerm.getDisplayName());
            label.setDescription(glossaryTerm.getDescription());
        } else {
            throw new IllegalArgumentException("Invalid source type " + label.getSource());
        }
    }

    /**
     * Returns true if the parent of the tag label is mutually exclusive
     */
    public boolean mutuallyExclusive(TagLabel label) {
//        String[] fqnParts = FullyQualifiedName.split(label.getTagFQN());
//        String parentFqn = FullyQualifiedName.getParentFQN(fqnParts);
//        boolean rootParent = fqnParts.length == 2;
//        if (label.getSource() == TagSource.CLASSIFICATION) {
//            return rootParent ? getClassification(parentFqn).getMutuallyExclusive()
//                    : getTag(parentFqn).getMutuallyExclusive();
//        } else if (label.getSource() == TagSource.GLOSSARY) {
//            return rootParent ? getGlossary(parentFqn).getMutuallyExclusive()
//                    : getGlossaryTerm(parentFqn).getMutuallyExclusive();
//        } else {
//            throw new IllegalArgumentException("Invalid source type " + label.getSource());
//        }
        return false;
    }

    public List<TagLabel> addDerivedTags(List<TagLabel> tagLabels) {
        if (CommonUtil.nullOrEmpty(tagLabels)) {
            return tagLabels;
        }

        // Filter out all the derived tags
        List<TagLabel> filteredTags =
                tagLabels.stream().filter(tag -> tag.getLabelType() != TagLabel.LabelType.DERIVED).toList();

        List<TagLabel> updatedTagLabels = new ArrayList<>();
        EntityUtil.mergeTags(updatedTagLabels, filteredTags);
        for (TagLabel tagLabel : tagLabels) {
            EntityUtil.mergeTags(updatedTagLabels, getDerivedTags(tagLabel));
        }
        updatedTagLabels.sort(EntityUtil.compareTagLabel);
        return updatedTagLabels;
    }

    private List<TagLabel> getDerivedTags(TagLabel tagLabel) {
//        if (tagLabel.getSource() == TagLabel.TagSource.GLOSSARY) { // Related tags are only supported for Glossary
//            List<TagLabel> derivedTags =
//                    repository.tagUsageDAO().getTags(tagLabel.getTagFQN());
//            derivedTags.forEach(tag -> tag.setLabelType(TagLabel.LabelType.DERIVED));
//            return derivedTags;
//        }
        return Collections.emptyList();
    }

    public List<TagLabel> getUniqueTags(List<TagLabel> tags) {
        Set<TagLabel> uniqueTags = new TreeSet<>(EntityUtil.compareTagLabel);
        uniqueTags.addAll(tags);
        return uniqueTags.stream().toList();
    }

    public void checkMutuallyExclusive(List<TagLabel> tagLabels) {
//        Map<String, TagLabel> map = new HashMap<>();
//        for (TagLabel tagLabel : CommonUtil.listOrEmpty(tagLabels)) {
//            // When two tags have the same parent that is mutuallyExclusive, then throw an error
//            String parentId = tagLabel.getParents();
//            TagLabel stored = map.put(parentId, tagLabel);
//            if (stored != null && mutuallyExclusive(tagLabel)) {
//                throw new IllegalArgumentException( String.format(
//                        "Tag labels %s and %s are mutually exclusive and can't be assigned together",
//                        tagLabel.getTagFQN(), stored.getTagFQN()));
//            }
//        }
    }

    public void checkDisabledTags(List<TagLabel> tagLabels) {
        for (TagLabel tagLabel : CommonUtil.listOrEmpty(tagLabels)) {
            if (tagLabel.getSource().equals(TagSource.CLASSIFICATION)) {
                Tag tag = getTag(tagLabel.getId());
                if (tag.getDisabled()) {
                    throw new IllegalArgumentException(String.format(
                            "Tag label [%s]/[%s] is disabled and can't be assigned to a data asset.", tag.getId(), tag.getName()));
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
        boolean failed = false;
        StringBuilder errorMessage = new StringBuilder();

        Map<String, List<TagLabel>> filteredTags =
                allAssetTags.entrySet().stream()
                        .filter(entry -> entry.getKey().startsWith(assetFqnHash))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Check Parent Tags
        List<TagLabel> parentTags = filteredTags.remove(assetFqnHash);

        if (parentTags != null) {
            List<TagLabel> tempList = new ArrayList<>(addDerivedTags(parentTags));
            tempList.addAll(glossaryTags);
            try {
                checkMutuallyExclusive(getUniqueTags(tempList));
            } catch (IllegalArgumentException ex) {
                failed = true;
                tempList.removeAll(glossaryTags);
                errorMessage.append(
                        String.format(
                                "Asset %s has a tag %s which is mutually exclusive with the one of the glossary tags %s. %n",
                                assetFqn,
                                converTagLabelArrayToString(tempList),
                                converTagLabelArrayToString(glossaryTags)));
            }
        }

        if (validateSubFields) {
            // Check SubFields Tags
            Set<TagLabel> subFieldTags =
                    filteredTags.values().stream().flatMap(List::stream).collect(Collectors.toSet());
            List<TagLabel> tempList = new ArrayList<>(addDerivedTags(subFieldTags.stream().toList()));
            tempList.addAll(glossaryTags);
            try {
                checkMutuallyExclusive(getUniqueTags(tempList));
            } catch (IllegalArgumentException ex) {
                failed = true;
                errorMessage.append(
                        String.format(
                                "Asset %s has a Subfield Column/Schema/Field containing tags %s which is mutually exclusive with the one of the glossary tags %s",
                                assetFqn,
                                converTagLabelArrayToString(tempList),
                                converTagLabelArrayToString(glossaryTags)));
            }
        }

        // Throw Exception if failed
        if (failed) {
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }

    public String converTagLabelArrayToString(List<TagLabel> tags) {
        return String.format(
                "[%s]", tags.stream().map(tag -> tag.getId().toString()).collect(Collectors.joining(", ")));
    }
}
