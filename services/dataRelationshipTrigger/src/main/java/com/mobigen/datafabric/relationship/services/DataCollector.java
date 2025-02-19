package com.mobigen.datafabric.relationship.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.datafabric.relationship.configurations.Configurations;
import com.mobigen.datafabric.relationship.data.FusionData;
import com.mobigen.datafabric.relationship.data.InteractionData;
import com.mobigen.datafabric.relationship.data.InteractionType;
import com.mobigen.datafabric.relationship.data.Metadata;
import com.mobigen.datafabric.relationship.dto.dolphin.FusionModel;
import com.mobigen.datafabric.relationship.dto.fabric.EntityRelationship;
import com.mobigen.datafabric.relationship.dto.fabric.*;
import com.mobigen.datafabric.relationship.fileWriter.CSVFileWriter;
import com.mobigen.datafabric.relationship.fileWriter.JSONFileWriter;
import com.mobigen.datafabric.relationship.models.exception.DataRelationshipException;
import com.mobigen.datafabric.relationship.repository.dolphin.FusionModelRepository;
import com.mobigen.datafabric.relationship.repository.fabric.*;
import com.mobigen.datafabric.relationship.utils.FullyQualifiedName;
import com.mobigen.datafabric.relationship.utils.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.openmetadata.schema.entity.classification.Tag;
import org.openmetadata.schema.entity.data.Container;
import org.openmetadata.schema.entity.data.GlossaryTerm;
import org.openmetadata.schema.entity.data.Table;
import org.openmetadata.schema.entity.teams.User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;

@Slf4j
@Service
public class DataCollector {
    // App Config
    private final Configurations configurations;
    // Repositories
    private final TableRepository tableRepository;
    private final EntityRelationshipRepository relationshipRepository;
    private final TagUsageRepository tagUsageRepository;
    private final TagRepository tagRepository;
    private final GlossaryTermEntityRepository glossaryTermRepository;
    private final UserEntityRepository userRepository;
    private final EntityExtensionRepository entityExtensionRepository;
    private final ProfilerDataEntityRepository profilerDataEntityRepository;
    private final FusionModelRepository fusionModelRepository;
    private final StorageContainerEntityRepository containerRepository;

    public DataCollector(Configurations configurations, TableRepository tableRepository,
                         EntityRelationshipRepository relationshipRepository,
                         TagUsageRepository tagUsageRepository,
                         TagRepository tagRepository,
                         GlossaryTermEntityRepository glossaryTermRepository,
                         UserEntityRepository userRepository,
                         EntityExtensionRepository entityExtensionRepository,
                         ProfilerDataEntityRepository profilerDataEntityRepository,
                         FusionModelRepository fusionModelRepository,
                         StorageContainerEntityRepository containerRepository) {
        this.configurations = configurations;
        this.tableRepository = tableRepository;
        this.relationshipRepository = relationshipRepository;
        this.tagUsageRepository = tagUsageRepository;
        this.tagRepository = tagRepository;
        this.glossaryTermRepository = glossaryTermRepository;
        this.userRepository = userRepository;
        this.entityExtensionRepository = entityExtensionRepository;
        this.profilerDataEntityRepository = profilerDataEntityRepository;
        this.fusionModelRepository = fusionModelRepository;
        this.containerRepository = containerRepository;
    }

    public List<String> collectMetaData(String strCurrentDateTime) throws DataRelationshipException {
        log.info("DataCollector.collectMetaData");

        // Table
        List<TableEntity> tables = tableRepository.findAll();
        List<Table> tableList = new ArrayList<>();
        for (TableEntity table : tables) {
            Table t = table.getJson();
            if (t.getDeleted()) continue;           // 삭제된 경우 수집하지 않음.
            switch (t.getServiceType()) {
                case Trino:
                    log.debug("Trino Table: Name[{}]", t.getName());
                    break;
                case Postgres, Mysql, MariaDB, Oracle:
                    log.debug("{} Table: Name[{}]", t.getServiceType().name(), t.getName());
                    break;
                default:
                    log.error("What Service?[{}] Table: FQN[{}]", t.getServiceType().name(), t.getFullyQualifiedName());
                    break;
            }
            t.setTags(derivedTags(getTagsByTable(t)));
            t.setFollowers(getFollowers(t.getId().toString(), Table.class.getSimpleName().toLowerCase()));
            t.setVotes(getVotes(t.getId().toString(), Table.class.getSimpleName().toLowerCase()));
            t.setSampleData(getSampleData(String.valueOf(t.getId()), "table"));
            t.setProfile(getTableProfile(t.getFullyQualifiedName(), "table.tableProfile"));
            t.setColumns(getColumnsProfile(t.getFullyQualifiedName(), t.getColumns(), "table.columnProfile"));
            // getLineage
            EntityLineage lineage = getLineage(t);
//            if (!lineage.getNodes().isEmpty()) {
//                log.info("Table[{}] Lineage: Up[{}] Down[{}]",
//                        t.getName(),
//                        lineage.getUpstreamEdges().size(), lineage.getDownstreamEdges().size());
//            }
            t.setExtension(lineage);
            tableList.add(t);
        }

        // Container
        List<StorageContainerEntity> containers = containerRepository.findAll();
        List<Container> containerList = new ArrayList<>();
        for (StorageContainerEntity container : containers) {
            Container c = container.getJson();
            if (c.getDeleted()) continue;                   // 삭제된 경우 수집하지 않음.
            if (c.getFileFormats().isEmpty()) continue;    // FileFormat 이 없는 경우 수집하지 않음(ex: 디렉토리)

            c.setTags(derivedTags(getTagsByTargetFQN(c.getFullyQualifiedName())));
            c.setFollowers(getFollowers(c.getId().toString(), Container.class.getSimpleName().toLowerCase()));
            c.setVotes(getVotes(c.getId().toString(), Container.class.getSimpleName().toLowerCase()));
            switch (c.getFileFormats().get(0)) {
                case Csv, Tsv, Xls, Xlsx:
                    c.setSampleData(getSampleData(String.valueOf(c.getId()), "container"));
                    break;
                case Doc, Docx:
                    break;
            }
            c.setProfile(getTableProfile(c.getFullyQualifiedName(), "container.tableProfile"));
            if( c.getDataModel() != null ) {
                ContainerDataModel dataModel = c.getDataModel();
                if( dataModel.getColumns() != null ) {
                    dataModel.setColumns(getColumnsProfile(c.getFullyQualifiedName(),
                            dataModel.getColumns(), "container.columnProfile"));
                    c.setDataModel(dataModel);
                }
            }
            // getLineage
            EntityLineage lineage = getLineage(c);
//            if (!lineage.getNodes().isEmpty()) {
//                log.info("Container[{}] Lineage: Up[{}] Down[{}]",
//                        c.getName(),
//                        lineage.getUpstreamEdges().size(), lineage.getDownstreamEdges().size());
//            }
            c.setExtension(lineage);
            containerList.add(c);
        }
        // Convert to JSON Object And Write To File
        try {
            return covertToMetadata(strCurrentDateTime, tableList, containerList);
        } catch (IOException e) {
            throw new DataRelationshipException("error in write json file", e);
        }
    }

    private List<String> covertToMetadata(String path, List<Table> tableList, List<Container> containerList) throws IOException {
        List<String> filePaths = new ArrayList<>();
        // Write to File
        String fullPath = PathUtil.combinePath(
                configurations.getTemporarySpace().getPath(), "/" + path);
        JSONFileWriter jsonFileWriter = new JSONFileWriter(fullPath);

        for( Table table : tableList ) {
            Metadata metadata = new Metadata();
            metadata.setId(table.getId());
            metadata.setName(table.getName());
            metadata.setDisplayName(table.getDisplayName());
            metadata.setDescription(table.getDescription());
            metadata.setUpdatedAt(table.getUpdatedAt());
            metadata.setUpdatedBy(table.getUpdatedBy());
            metadata.setDataType("structured");
            if( table.getServiceType().name().equalsIgnoreCase("trino") ) {
                metadata.setTableType("fusion");
            } else {
                metadata.setTableType(table.getTableType().toString().toLowerCase());
            }
            metadata.setFileFormat(null);
            metadata.setFullPath(null);
            metadata.setSize(null);
            metadata.setColumns(columnsConvert(table.getColumns()));
            metadata.setTableConstraints(table.getTableConstraints());
            metadata.setOwner(table.getOwner() != null ? table.getOwner().getId() : null);
            metadata.setDatabaseSchema(referenceConvert(table.getDatabaseSchema()));
            metadata.setDatabase(referenceConvert(table.getDatabase()));
            metadata.setService(referenceConvert(table.getService()));
            metadata.setServiceType(table.getServiceType().toString());
            metadata.setSchemaDefinition(table.getSchemaDefinition());
            metadata.setTags(table.getTags());
            metadata.setFollowers(table.getFollowers());
            metadata.setVotes(table.getVotes());
            metadata.setSampleData(table.getSampleData());
            metadata.setProfile(table.getProfile());
            metadata.setLineage((EntityLineage) table.getExtension());

            String writeFilePath = jsonFileWriter.writeObjectToJsonFile(metadata);
            filePaths.add(writeFilePath);
        }

        for( Container container : containerList ) {
            Metadata metadata = new Metadata();
            metadata.setId(container.getId());
            metadata.setName(container.getName());
            metadata.setDisplayName(container.getDisplayName());
            metadata.setDescription(container.getDescription());
            metadata.setUpdatedAt(container.getUpdatedAt());
            metadata.setUpdatedBy(container.getUpdatedBy());
            metadata.setFileFormat(container.getFileFormats().get(0).toString().toLowerCase());
            switch(container.getFileFormats().get(0)) {
                case Csv, Tsv, Xls, Xlsx:
                    metadata.setDataType("structured");
                    metadata.setTableType("regular");
                    break;
                case Doc, Docx, Hwp, Hwpx:
                    metadata.setDataType("unstructured");
                    metadata.setTableType(null);
                    break;
                default:
                    metadata.setDataType("unknown");
                    metadata.setTableType(null);
                    break;
            }
            metadata.setFullPath(container.getFullPath());
            metadata.setSize(container.getSize());
            if( container.getDataModel() != null && container.getDataModel().getColumns() != null ) {
                metadata.setColumns(columnsConvert(container.getDataModel().getColumns()));
            } else {
                metadata.setColumns(null);
            }
            metadata.setTableConstraints(null);
            metadata.setOwner(container.getOwner() != null ? container.getOwner().getId() : null);
            metadata.setDatabaseSchema(null);
            metadata.setDatabase(null);
            metadata.setService(referenceConvert(container.getService()));
            metadata.setServiceType(container.getServiceType().toString());
            metadata.setSchemaDefinition(null);
            metadata.setTags(container.getTags());
            metadata.setFollowers(container.getFollowers());
            metadata.setVotes(container.getVotes());
            metadata.setSampleData(container.getSampleData());
            metadata.setProfile(container.getProfile());
            metadata.setLineage((EntityLineage) container.getExtension());

            String writeFilePath = jsonFileWriter.writeObjectToJsonFile(metadata);
            filePaths.add(writeFilePath);
        }
        return filePaths;
    }

    private List<Column> columnsConvert(List<Column> columns) {
        List<Column> ret = new ArrayList<>();
        for( Column column : columns ) {
            column.setFullyQualifiedName(null);
            column.setDataTypeDisplay(null);
            ret.add(column);
        }
        return ret;
    }

    private EntityReference referenceConvert(EntityReference reference) {
        if (reference == null) {
            return null;
        }
        reference.setFullyQualifiedName(null);
        reference.setDisplayName(null);
        reference.setDeleted(null);
        return reference;
    }

    private List<TagLabel> getTagsByTable(Table table) {
        return getTagsByTargetFQN(table.getFullyQualifiedName());
    }

    private List<TagLabel> getTagsByTargetFQN(String targetFQN) {
        log.debug("Get Tags From Data : {}", targetFQN);
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

    private List<EntityReference> getFollowers(String id, String entityType) {
        log.debug("Get Follower Type[{}] ID[{}]", entityType, id);
        List<EntityRelationship> relations =
                relationshipRepository.findFrom(id, entityType,
                        Relationship.FOLLOWS.ordinal(), User.class.getSimpleName().toLowerCase());
        List<EntityReference> followers = new ArrayList<>();
        for (EntityRelationship relation : relations) {
            EntityReference follower = new EntityReference();
            follower.setId(UUID.fromString(relation.getId().getFromId()));
            follower.setType(User.class.getSimpleName().toLowerCase());
            followers.add(follower);
        }
        return followers;
    }

    private Votes getVotes(String id, String entityType) {
        log.debug("Get Votes Type[{}] ID[{}]", entityType, id);
        List<EntityRelationship> upVotes = new ArrayList<>();
        List<EntityRelationship> downVotes = new ArrayList<>();
        List<EntityRelationship> relations = relationshipRepository.
                findFrom(id, entityType, Relationship.VOTED.ordinal(), User.class.getSimpleName().toLowerCase());

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
        for (EntityRelationship relation : relationshipList) {
            Optional<UserEntity> entity = userRepository.findById(relation.getId().getFromId());
            if (entity.isPresent()) {
                UserEntity userEntity = entity.get();
                User user = userEntity.getJson();
                EntityReference reference = new EntityReference();
                reference.setId(user.getId());
                reference.setName(user.getName());
//                reference.setDescription(user.getDescription());
//                reference.setFullyQualifiedName(user.getFullyQualifiedName());
//                reference.setDisplayName(user.getDisplayName());
//                reference.setDeleted(user.getDeleted());
                referenceList.add(reference);
            }
        }
        if (!referenceList.isEmpty()) {
            log.debug("find user");
        }
        return referenceList;
    }

    private TableData getSampleData(String id, String extensionPrefix) {
        log.debug("Get SampleData ID[{}] Extension[{}]", id, extensionPrefix);
        List<EntityExtension> extensions = entityExtensionRepository.getExtensions(id, extensionPrefix);
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

    private TableProfile getTableProfile(String fqn, String extension) {
        log.debug("Get TableProfile FQN[{}] Extension[{}]", fqn, extension);
        Optional<String> opt =
                profilerDataEntityRepository.findProfilerDataEntity(
                        FullyQualifiedName.buildHash(fqn), extension);
        if (opt.isPresent()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.convertValue(opt.get(), TableProfile.class);
        }
        return null;
    }

    private List<Column> getColumnsProfile(String fqn, List<Column> columns, String extension) {
        log.debug("Get Columns Profile FQN[{}] Extension[{}]", fqn, extension);
        List<Column> ret = new ArrayList<>();
        for (Column c : columns) {
            log.debug("Get Column Profile : {}", c.getName());
            c.setProfile(getColumnProfile(c, extension));
            ret.add(c);
        }
        return ret;
    }

    private ColumnProfile getColumnProfile(Column c, String extension) {
        Optional<String> opt =
                profilerDataEntityRepository.findProfilerDataEntity(
                        FullyQualifiedName.buildHash(c.getFullyQualifiedName()), extension);
        if (opt.isPresent()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.convertValue(opt.get(), ColumnProfile.class);
        }
        return null;
    }

    private EntityLineage getLineage(Table t) {
        EntityReference ref = new EntityReference()
                .withId(t.getId())
                .withName(t.getName())
                .withType(Table.class.getSimpleName().toLowerCase());
//                .withFullyQualifiedName(t.getFullyQualifiedName())
//                .withDescription(t.getDescription());
        return getLineage(ref);
    }

    private EntityLineage getLineage(Container c) {
        EntityReference ref = new EntityReference()
                .withId(c.getId())
                .withName(c.getName())
                .withType(Container.class.getSimpleName().toLowerCase());
//                .withFullyQualifiedName(c.getFullyQualifiedName())
//                .withDescription(c.getDescription());
        return getLineage(ref);
    }

    private EntityLineage getLineage(EntityReference ref) {
        log.debug("Get Lineage FQN[{}] EntityType[{}]", ref.getFullyQualifiedName(), ref.getType());
        EntityLineage lineage = new EntityLineage()
                .withEntity(ref)
                .withNodes(new ArrayList<>())
                .withUpstreamEdges(new ArrayList<>())
                .withDownstreamEdges(new ArrayList<>());
        getUpstreamLineage(ref.getId().toString(), ref.getType(), lineage, 3);
        getDownstreamLineage(ref.getId().toString(), ref.getType(), lineage, 3);

        // Remove Duplicate nodes
        lineage.withNodes(lineage.getNodes().stream().distinct().toList());
        return lineage;
    }

    private void getDownstreamLineage(String id, String entityType, EntityLineage lineage, int downstreamDepth) {
        if (downstreamDepth <= 0) {
            return;
        }
        List<EntityRelationship> records = relationshipRepository.findTo(id, entityType, Relationship.UPSTREAM.ordinal());
        final List<EntityReference> downstreamEntityReferences = new ArrayList<>();
        for (EntityRelationship record : records) {
            EntityReference ref = getEntityReferenceById(record.getId().getToId(), record.getToEntity());
            // Get ReferenceEntity
            if (ref == null) {
                log.error("Reference Entity is null");
                continue;
            }
            LineageDetails lineageDetails = new ObjectMapper().convertValue(record.getJson(), LineageDetails.class);
            downstreamEntityReferences.add(ref);
            lineage
                    .getDownstreamEdges()
                    .add(
                            new Edge()
                                    .withToEntity(ref.getId())
                                    .withFromEntity(UUID.fromString(id))
                                    .withLineageDetails(lineageDetails));
        }
        lineage.getNodes().addAll(downstreamEntityReferences);

        downstreamDepth--;
        // Recursively add upstream nodes and edges
        for (EntityReference entity : downstreamEntityReferences) {
            getDownstreamLineage(entity.getId().toString(), entity.getType(), lineage, downstreamDepth);
        }
    }

    private void getUpstreamLineage(String id, String entityType, EntityLineage lineage, int upstreamDepth) {
        if (upstreamDepth <= 0) {
            return;
        }
        List<EntityRelationship> records = relationshipRepository.findFrom(id, entityType, Relationship.UPSTREAM.ordinal());
        ObjectMapper objectMapper = new ObjectMapper();
        final List<EntityReference> upstreamEntityReferences = new ArrayList<>();
        for (EntityRelationship record : records) {
            EntityReference ref = getEntityReferenceById(record.getId().getFromId(), record.getFromEntity());
            // Get ReferenceEntity
            if (ref == null) {
                log.error("Reference Entity is null");
                continue;
            }
            LineageDetails lineageDetails =
                    objectMapper.convertValue(record.getJson(), LineageDetails.class);
            upstreamEntityReferences.add(ref);
            lineage.getUpstreamEdges()
                    .add(
                            new Edge()
                                    .withFromEntity(ref.getId())
                                    .withToEntity(UUID.fromString(id))
                                    .withLineageDetails(lineageDetails));

        }
        lineage.getNodes().addAll(upstreamEntityReferences);

        upstreamDepth--;
        // Recursively add upstream nodes and edges
        for (EntityReference entity : upstreamEntityReferences) {
            getUpstreamLineage(entity.getId().toString(), entity.getType(), lineage, upstreamDepth);
        }
    }

    public EntityReference getEntityReferenceById(String id, String entityType) {
        switch (entityType) {
            case "table":
                log.debug("Get Reference(Table): {}", id);
                Optional<TableEntity> optTable = tableRepository.findById(id);
                if (optTable.isPresent()) {
                    Table table = optTable.get().getJson();
                    EntityReference ref = new EntityReference();
                    ref.setId(table.getId());
                    ref.setType(table.getClass().getSimpleName().toLowerCase());
                    ref.setName(table.getName());
                    ref.setDisplayName(table.getDisplayName());
//                    ref.setFullyQualifiedName(table.getFullyQualifiedName());
//                    ref.setDescription(table.getDescription());
                    return ref;
                }
                break;
            case "container":
                log.debug("Get Reference(Container) : {}", id);
                Optional<StorageContainerEntity> optContainer = containerRepository.findById(id);
                if (optContainer.isPresent()) {
                    Container container = optContainer.get().getJson();
                    EntityReference ref = new EntityReference();
                    ref.setId(container.getId());
                    ref.setType(container.getClass().getSimpleName().toLowerCase());
                    ref.setName(container.getName());
                    ref.setDisplayName(container.getDisplayName());
//                    ref.setFullyQualifiedName(container.getFullyQualifiedName());
//                    ref.setDescription(container.getDescription());
                    return ref;
                }
                break;
            default:
                log.error("Unknown Entity Type: {}", entityType);
                break;
        }
        return null;
    }

    public String collectUserData(String path) throws DataRelationshipException {
        log.info("Collect user's favorites and recommendations and save them as CSV files.");
        ObjectMapper objectMapper = new ObjectMapper();
        CSVFileWriter interactionWriter = new CSVFileWriter();
        String fileName = String.format("%s/interaction.csv", path);
        String fullPath;
        try {
            fullPath = interactionWriter.init(configurations.getTemporarySpace().getPath(),
                    fileName, InteractionData.columns);
            // 사용자 목록 조회
            List<UserEntity> userEntities = userRepository.findAll();
            for (UserEntity userEntity : userEntities) {
                User user = objectMapper.convertValue(userEntity.getJson(), User.class);
                if (user.getDeleted()) continue;           // 삭제된 경우 수집하지 않음.
                if (user.getIsBot()) continue;
                log.debug("User: FQN[{}]", user.getFullyQualifiedName());
                // 사용자가 팔로우하는 테이블 데이터 검색
                List<EntityRelationship> followTableList = relationshipRepository.
                        findTo(user.getId().toString(), "user", Relationship.FOLLOWS.ordinal(), "table");
                // 사용자가 팔로우하는 컨테이너 데이터 검색
                List<EntityRelationship> followContainerList = relationshipRepository.
                        findTo(user.getId().toString(), "user", Relationship.FOLLOWS.ordinal(), "container");
                // 사용자가 추천/비추천한 테이블 데이터 검색
                List<EntityRelationship> voteTableList = relationshipRepository.
                        findTo(user.getId().toString(), "user", Relationship.VOTED.ordinal(), "table");
                // 사용자가 추천/비추천한 컨테이너 데이터 검색
                List<EntityRelationship> voteContainerList = relationshipRepository.
                        findTo(user.getId().toString(), "user", Relationship.VOTED.ordinal(), "container");

                follow(interactionWriter, user, followTableList);
                follow(interactionWriter, user, followContainerList);
                recommend(interactionWriter, user, voteTableList);
                recommend(interactionWriter, user, voteContainerList);
            }
        } catch (IOException e) {
            log.error("Error while initializing DataWriter For User : {}", e.getMessage());
            throw new DataRelationshipException("Error while initializing DataWriter For User", e);
        } finally {
            interactionWriter.close();
        }
        return fullPath;
    }

    private void follow(CSVFileWriter interactionWriter, User user, List<EntityRelationship> followList) {
        if (!followList.isEmpty()) {
            followList.forEach(relation -> {
                InteractionData interactionData = InteractionData.builder()
                        .userId(user.getId().toString())
                        .dataId(relation.getId().getToId())
                        .type(InteractionType.FOLLOW)
                        .value(0)
                        .build();
                interactionWriter.write(interactionData.getData());
            });
        }
    }

    private void recommend(CSVFileWriter interactionWriter, User user, List<EntityRelationship> voteList) {
        if (!voteList.isEmpty()) {
            voteList.forEach(relation -> {
                if (relation.getJson().equals("\"votedUp\"")) {
                    InteractionData interactionData = InteractionData.builder()
                            .userId(user.getId().toString())
                            .dataId(relation.getId().getToId())
                            .type(InteractionType.RECOMMEND)
                            .value(1)
                            .build();
                    interactionWriter.write(interactionData.getData());
                } else if (relation.getJson().equals("\"voteDown\"")) {
                    InteractionData interactionData = InteractionData.builder()
                            .userId(user.getId().toString())
                            .dataId(relation.getId().getToId())
                            .type(InteractionType.RECOMMEND)
                            .value(0)
                            .build();
                    interactionWriter.write(interactionData.getData());
                }
            });
        }
    }

    public String collectFusionData(String path) {
        log.info("Collect Fusion History and save them as CSV files.");
        CSVFileWriter interactionWriter = new CSVFileWriter();
        String fileName = String.format("%s/fusion-history.csv", path);
        String fullPath;
        try {
            fullPath = interactionWriter.init(configurations.getTemporarySpace().getPath(),
                    fileName, FusionData.columns);
            // 융합 데이터 조회
            Iterable<FusionModel> fusionEntities = fusionModelRepository.findAll();
            // JobID를 기준으로 DataID 정리
            Map<String, List<String>> fusionDataMap = new HashMap<>();
            fusionEntities.forEach(fusionEntity -> {
                List<String> dataIds = fusionDataMap.get(fusionEntity.getJobId());
                if (dataIds == null) {
                    dataIds = new ArrayList<>();
                    dataIds.add(fusionEntity.getModelidofom().toString());
                } else {
                    // 하나의 데이터를 이용한 조인 기록이 있어 제외될 수 있도록 중복 체크
                    if (!dataIds.contains(fusionEntity.getModelidofom().toString()))
                        dataIds.add(fusionEntity.getModelidofom().toString());
                }
                fusionDataMap.put(fusionEntity.getJobId(), dataIds);
            });
            // 정제 기록과 중복 제거
            removeDuplicateValues(fusionDataMap);
            // 파일 작성
            fusionDataMap.forEach((jobId, dataIds) -> {
                dataIds.forEach(dataId -> {
                    FusionData fusionData = FusionData.builder()
                            .dataId(dataId)
                            .queryId(jobId)
                            .build();
                    interactionWriter.write(fusionData.getData());
                });
            });
        } catch (IOException e) {
            log.error("Error while initializing DataWriter For Fusion : {}", e.getMessage());
            return null;
        } finally {
            interactionWriter.close();
        }
        return fullPath;
    }

    public void removeDuplicateValues(Map<String, List<String>> data) {
        // 중복을 체크할 Set 생성
        Set<List<String>> uniqueValues = new HashSet<>();

        // 중복을 제거한 Map을 저장할 Iterator
        Iterator<Map.Entry<String, List<String>>> iterator = data.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next();
            List<String> value = entry.getValue();
            // 값이 1개 이하인 경우는 융합이 아닌 정제이므로 제거
            if (value.size() <= 1) {
                iterator.remove();
                continue;
            }

            // 중복 제거
            if (!uniqueValues.add(value)) {
                iterator.remove(); // 중복 항목 제거
            }
        }
    }
}
