package com.mobigen.datafabric.relationship.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.datafabric.relationship.dto.fabric.EntityRelationship;
import com.mobigen.datafabric.relationship.dto.fabric.*;
import com.mobigen.datafabric.relationship.repository.fabric.*;
import com.mobigen.datafabric.relationship.utils.FullyQualifiedName;
import lombok.extern.slf4j.Slf4j;
import org.openmetadata.schema.entity.classification.Tag;
import org.openmetadata.schema.entity.data.GlossaryTerm;
import org.openmetadata.schema.entity.data.Table;
import org.openmetadata.schema.entity.teams.User;
import org.openmetadata.schema.type.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiPredicate;

@Slf4j
@Service
public class DataCollector {
    private final TableRepository tableRepository;
    private final EntityRelationshipRepository relationshipRepository;
    private final TagUsageRepository tagUsageRepository;
    private final TagRepository tagRepository;
    private final GlossaryTermEntityRepository glossaryTermRepository;
    private final UserEntityRepository userRepository;
    private final EntityExtensionRepository entityExtensionRepository;
    private final ProfilerDataEntityRepository profilerDataEntityRepository;

    public DataCollector(TableRepository tableRepository, EntityRelationshipRepository relationshipRepository,
                         TagUsageRepository tagUsageRepository, TagRepository tagRepository,
                         GlossaryTermEntityRepository glossaryTermRepository, UserEntityRepository userRepository,
                         EntityExtensionRepository entityExtensionRepository,
                         ProfilerDataEntityRepository profilerDataEntityRepository) {
        this.tableRepository = tableRepository;
        this.relationshipRepository = relationshipRepository;
        this.tagUsageRepository = tagUsageRepository;
        this.tagRepository = tagRepository;
        this.glossaryTermRepository = glossaryTermRepository;
        this.userRepository = userRepository;
        this.entityExtensionRepository = entityExtensionRepository;
        this.profilerDataEntityRepository = profilerDataEntityRepository;
    }

    public void collectData() {
        log.info("DataCollector.collectData");

        ObjectMapper objectMapper = new ObjectMapper();

        List<TableEntity> tables = tableRepository.findAll();
        List<Table> tableList = new ArrayList<>();
        for (TableEntity table : tables) {
            Table t = objectMapper.convertValue(table.getJson(), Table.class);
            if (t.getDeleted()) continue;           // 삭제된 경우 수집하지 않음.
            tableList.add(t);
        }
        for (Table t : tableList) {
            switch (t.getServiceType()) {
                case Trino:
                    log.info("Trino Table: FQN[{}]", t.getFullyQualifiedName());
                    break;
                case Postgres, Mysql, MariaDB, Oracle:
                    log.info("{} Table: FQN[{}]", t.getServiceType().name(), t.getFullyQualifiedName());
                    break;
                default:
                    log.info("What Service?[{}] Table: FQN[{}]", t.getServiceType().name(), t.getFullyQualifiedName());
                    break;
            }
            t.setTags(derivedTags(getTagsByTable(t)));
            t.setFollowers(getFollowers(t));
            t.setVotes(getVotes(t));
            t.setSampleData(getSampleData(t));
            t.setProfile(getTableProfile(t));
            t.setColumns(getColumnsProfile(t));
            // getLineage
//            t.setExtension(getLineage(t));
        }
    }

    private List<TagLabel> getTagsByTable(Table table) {
        return getTagsByTargetFQN(table.getFullyQualifiedName());
    }

    private List<TagLabel> getTagsByTargetFQN(String targetFQN) {
        log.debug("Get Tags by {}", targetFQN);
        List<TagUsage> tagUsages = tagUsageRepository.findTagUsageById_TargetFQNHash(FullyQualifiedName.buildHash(targetFQN));
        List<TagLabel> tagLabels = new ArrayList<>();
        for (TagUsage tagUsage : tagUsages) {
            TagLabel label = applyTagCommonFields(tagUsage);
            if (label != null) {
                tagLabels.add(label);
            }
        }
        return tagLabels;
    }

    private TagLabel applyTagCommonFields(TagUsage tagUsage) {
        ObjectMapper objectMapper = new ObjectMapper();
        TagLabel label = new TagLabel();
        label.setSource(tagUsage.getId().getSource());
        label.setTagFQN(tagUsage.getTagFQN());
        label.setLabelType(tagUsage.getLabelType());
        label.setState(tagUsage.getState());
        if (tagUsage.getId().getSource() == TagLabel.TagSource.CLASSIFICATION) {
            TagEntity entity = tagRepository.findTagByFqnHash(FullyQualifiedName.buildHash(tagUsage.getTagFQN()));
            if (entity == null) {
                log.warn("Tag[{}] is not found", tagUsage.getTagFQN());
                return null;
            }
            Tag tag = objectMapper.convertValue(entity.getJson(), Tag.class);
            label.setName(tag.getName());
            label.setDisplayName(tag.getDisplayName());
            label.setDescription(tag.getDescription());
        } else if (tagUsage.getId().getSource() == TagLabel.TagSource.GLOSSARY) {
            GlossaryTermEntity entity = this.glossaryTermRepository
                    .findGlossaryTermEntityByFqnHash(FullyQualifiedName.buildHash(tagUsage.getTagFQN()));
            if (entity == null) {
                log.warn("GlossaryTerm[{}] is not found", tagUsage.getTagFQN());
                return null;
            }
            GlossaryTerm term = objectMapper.convertValue(entity.getJson(), GlossaryTerm.class);
            label.setName(term.getName());
            label.setDisplayName(term.getDisplayName());
            label.setDescription(term.getDescription());
        } else {
            log.error("TagUsage[{}] Invalid source type {}",
                    tagUsage.getTagFQN(), tagUsage.getId().getSource());
            return null;
        }
        return label;
    }

    public static final BiPredicate<TagLabel, TagLabel> tagLabelMatch =
            (tag1, tag2) -> tag1.getTagFQN().equals(tag2.getTagFQN()) && tag1.getSource().equals(tag2.getSource());

    public static void mergeTags(List<TagLabel> mergeTo, List<TagLabel> mergeFrom) {
        if (mergeFrom == null || mergeFrom.isEmpty()) {
            return;
        }
        for (TagLabel fromTag : mergeFrom) {
            TagLabel tag =
                    mergeTo.stream().filter(t -> tagLabelMatch.test(t, fromTag)).findAny().orElse(null);
            if (tag == null) { // The tag does not exist in the mergeTo list. Add it.
                mergeTo.add(fromTag);
            }
        }
    }

    private List<TagLabel> derivedTags(List<TagLabel> tagLabelList) {
        if (tagLabelList == null || tagLabelList.isEmpty()) {
            return tagLabelList;
        }
        List<TagLabel> filteredTags = tagLabelList.stream()
                .filter(tag -> tag.getLabelType() != TagLabel.LabelType.DERIVED).toList();
        // 중복 제거
        List<TagLabel> updatedTagLabels = new ArrayList<>();
        mergeTags(updatedTagLabels, filteredTags);
        for (TagLabel tagLabel : tagLabelList) {
            mergeTags(updatedTagLabels, getDerivedTags(tagLabel));
        }
        return updatedTagLabels;
    }

    private List<TagLabel> getDerivedTags(TagLabel tagLabel) {
        if (tagLabel.getSource() == TagLabel.TagSource.GLOSSARY) { // Related tags are only supported for Glossary
            List<TagLabel> derivedTags =
                    getTagsByTargetFQN(tagLabel.getTagFQN());
            derivedTags.forEach(tag -> tag.setLabelType(TagLabel.LabelType.DERIVED));
            return derivedTags;
        }
        return Collections.emptyList();
    }

    private List<EntityReference> getFollowers(Table t) {
        log.debug("Get Follower by {}", t.getFullyQualifiedName());
        List<EntityRelationship> relations =
                relationshipRepository.findFollowers("user", t.getId().toString(), Relationship.FOLLOWS.ordinal());
        List<EntityReference> followers = new ArrayList<>();
        for (EntityRelationship relation : relations) {
            EntityReference follower = new EntityReference();
            follower.setId(UUID.fromString(relation.getId().getFromId()));
            follower.setType(User.class.getName());
            followers.add(follower);
        }
        return followers;
    }

    private Votes getVotes(Table t) {
        log.debug("Get Votes by {}", t.getFullyQualifiedName());
        List<EntityRelationship> upVotes = new ArrayList<>();
        List<EntityRelationship> downVotes = new ArrayList<>();
        List<EntityRelationship> relations = relationshipRepository.
                findFrom(t.getId().toString(), "table", Relationship.VOTED.ordinal(), "user");

        ObjectMapper objectMapper = new ObjectMapper();
        for (EntityRelationship relation : relations) {
            if (relation.getJson().equals("\"votedUp\"")) {
                upVotes.add(relation);
            } else if (relation.getJson().equals("\"votedDown\"")) {
                downVotes.add(relation);
            }
        }
        List<EntityReference> upVoters = ConvertReferences(upVotes);
        List<EntityReference> downVoters = ConvertReferences(downVotes);
        return new Votes()
                .withUpVotes(upVoters.size())
                .withDownVotes(downVoters.size())
                .withUpVoters(upVoters)
                .withDownVoters(downVoters);
    }

    private List<EntityReference> ConvertReferences(List<EntityRelationship> relationshipList) {
        List<EntityReference> referenceList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (EntityRelationship relation : relationshipList) {
            Optional<UserEntity> entity = userRepository.findById(relation.getId().getFromId());
            if (entity.isPresent()) {
                UserEntity userEntity = entity.get();
                User user = objectMapper.convertValue(userEntity.getJson(), User.class);
                EntityReference reference = new EntityReference();
                reference.setId(user.getId());
                reference.setName(user.getName());
                reference.setFullyQualifiedName(user.getFullyQualifiedName());
                reference.setDescription(user.getDescription());
                reference.setDisplayName(user.getDisplayName());
                reference.setDeleted(user.getDeleted());
                referenceList.add(reference);
            }
        }
        if (!referenceList.isEmpty()) {
            log.debug("find user");
        }
        return referenceList;
    }

    private TableData getSampleData(Table t) {
        log.debug("Get SampleData by {}", t.getFullyQualifiedName());
        List<EntityExtension> extensions = entityExtensionRepository.getExtensions(t.getId().toString(), "table");
        if (extensions == null || extensions.isEmpty()) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        for (EntityExtension extension : extensions) {
            if (extension.getJsonSchema().equals("tableData")) {
                return objectMapper.convertValue(extension.getJson(), TableData.class);
            }
        }
        return null;
    }

    private TableProfile getTableProfile(Table t) {
        log.debug("Get TableProfile: {}", t.getFullyQualifiedName());
        Optional<String> opt =
                profilerDataEntityRepository.findProfilerDataEntity(
                        FullyQualifiedName.buildHash(t.getFullyQualifiedName()), "table.tableProfile");
        if (opt.isPresent()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.convertValue(opt.get(), TableProfile.class);
        }
        return null;
    }

    private List<Column> getColumnsProfile(Table t) {
        log.debug("Get Columns Profile: {}", t.getFullyQualifiedName());
        List<Column> columns = new ArrayList<>();
        for( Column c : t.getColumns() ) {
            log.info("Get Column Profile: {}", c.getName());
            c.setProfile(getColumnProfile(c));
            columns.add(c);
        }
        return columns;
    }

    private ColumnProfile getColumnProfile(Column c) {
        Optional<String> opt =
                profilerDataEntityRepository.findProfilerDataEntity(
                        FullyQualifiedName.buildHash(c.getFullyQualifiedName()), "table.columnProfile");
        if (opt.isPresent()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.convertValue(opt.get(), ColumnProfile.class);
        }
        return null;
    }
}
