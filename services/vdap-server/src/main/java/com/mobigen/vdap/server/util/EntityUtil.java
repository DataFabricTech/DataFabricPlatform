package com.mobigen.vdap.server.util;

import com.mobigen.vdap.common.utils.CommonUtil;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.entity.data.GlossaryTerm;
import com.mobigen.vdap.schema.entity.data.TermReference;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.Field;
import com.mobigen.vdap.schema.type.TagLabel;
import com.mobigen.vdap.schema.type.TagLabel.TagSource;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.models.EntityVersionPair;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Slf4j
public final class EntityUtil {
    //
    // Comparators used for sorting list based on the given type
    //
    public static final Comparator<EntityReference> compareEntityReference =
            Comparator.comparing(EntityReference::getName);
    public static final Comparator<EntityReference> compareEntityReferenceById =
            Comparator.comparing(EntityReference::getId).thenComparing(EntityReference::getType);
    public static final Comparator<EntityVersionPair> compareVersion =
            Comparator.comparing(EntityVersionPair::getVersion);
    public static final Comparator<TagLabel> compareTagLabel =
            Comparator.comparing(TagLabel::getName);
    //    public static final Comparator<TableConstraint> compareTableConstraint =
//      Comparator.comparing(TableConstraint::getConstraintType);
//  public static final Comparator<ChangeEvent> compareChangeEvent =
//      Comparator.comparing(ChangeEvent::getTimestamp);
    public static final Comparator<GlossaryTerm> compareGlossaryTerm =
            Comparator.comparing(GlossaryTerm::getName);

    //
    // Matchers used for matching two items in a list
    //
    public static final BiPredicate<Object, Object> objectMatch = Object::equals;

    public static final BiPredicate<EntityReference, EntityReference> entityReferenceMatch =
            (ref1, ref2) -> ref1.getId().equals(ref2.getId()) && ref1.getType().equals(ref2.getType());

    public static final BiPredicate<TagLabel, TagLabel> tagLabelMatch =
            (tag1, tag2) ->
                    tag1.getId().equals(tag2.getId()) &&
                            tag1.getSource().equals(tag2.getSource());

    public static final BiPredicate<String, String> stringMatch = String::equals;

//  public static final BiPredicate<Column, Column> columnMatch =
//      (column1, column2) ->
//          column1.getName().equalsIgnoreCase(column2.getName())
//              && column1.getDataType() == column2.getDataType()
//              && column1.getArrayDataType() == column2.getArrayDataType();

//  public static final BiPredicate<Column, Column> columnNameMatch =
//      (column1, column2) -> column1.getName().equalsIgnoreCase(column2.getName());

//  public static final BiPredicate<TableConstraint, TableConstraint> tableConstraintMatch =
//      (constraint1, constraint2) ->
//          constraint1.getConstraintType() == constraint2.getConstraintType()
//              && constraint1.getColumns().equals(constraint2.getColumns())
//              && ((constraint1.getReferredColumns() == null
//                      && constraint2.getReferredColumns() == null)
//                  || (constraint1.getReferredColumns().equals(constraint2.getReferredColumns())));


//  public static final BiPredicate<GlossaryTerm, GlossaryTerm> glossaryTermMatch =
//      (filter1, filter2) -> filter1.getFullyQualifiedName().equals(filter2.getFullyQualifiedName());

    //  public static final BiPredicate<ContainerFileFormat, ContainerFileFormat>
//      containerFileFormatMatch = Enum::equals;
    public static final BiPredicate<TermReference, TermReference> termReferenceMatch =
            (ref1, ref2) ->
                    ref1.getName().equals(ref2.getName()) && ref1.getEndpoint().equals(ref2.getEndpoint());

    public static final BiPredicate<Field, Field> schemaFieldMatch =
            (field1, field2) ->
                    field1.getName().equalsIgnoreCase(field2.getName())
                            && field1.getDataType() == field2.getDataType();

//  public static final BiPredicate<SearchIndexField, SearchIndexField> searchIndexFieldMatch =
//      (field1, field2) ->
//          field1.getName().equalsIgnoreCase(field2.getName())
//              && field1.getDataType() == field2.getDataType();

    /**
     * Validate that JSON payload can be turned into POJO object
     */
    public static <T> T validate(Object id, String json, Class<T> clz)
            throws Exception {
        T entity = null;
        if (json != null) {
            entity = JsonUtils.readValue(json, clz);
        }
        if (entity == null) {
            throw new CustomException(String.format("%s instance for %s not found", clz.getSimpleName(), id.toString()), id);
        }
        return entity;
    }

//    public static List<EntityReference> populateEntityReferences(List<EntityReference> list) {
//        if (list != null) {
//            for (EntityReference ref : list) {
//                EntityReference ref2 = Entity.getEntityReference(ref, Include.ALL);
//                EntityUtil.copy(ref2, ref);
//            }
//            list.sort(compareEntityReference);
//        }
//        return list;
//    }
//
//    public static List<EntityReference> getEntityReferences(List<EntityRelationshipRecord> list) {
//        if (CommonUtil.nullOrEmpty(list)) {
//            return Collections.emptyList();
//        }
//        List<EntityReference> refs = new ArrayList<>();
//        for (EntityRelationshipRecord ref : list) {
//            refs.add(Entity.getEntityReferenceById(ref.getType(), ref.getId(), Include.ALL));
//        }
//        refs.sort(compareEntityReference);
//        return refs;
//    }
//
//    public static List<EntityReference> populateEntityReferencesById(
//            List<UUID> list, String entityType) {
//        List<EntityReference> refs = toEntityReferences(list, entityType);
//        return populateEntityReferences(refs);
//    }

//  public static EntityReference validateEntityLink(EntityLink entityLink) {
//    String entityType = entityLink.getEntityType();
//    String fqn = entityLink.getEntityFQN();
//    return Entity.getEntityReferenceByName(entityType, fqn, Include.ALL);
//  }

//  public static UsageDetails getLatestUsage(UsageDAO usageDAO, UUID entityId) {
//    LOG.debug("Getting latest usage for {}", entityId);
//    UsageDetails details = usageDAO.getLatestUsage(entityId.toString());
//    if (details == null) {
//      LOG.debug("Usage details not found. Sending default usage");
//      UsageStats stats = new UsageStats().withCount(0).withPercentileRank(0.0);
//      details =
//          new UsageDetails()
//              .withDailyStats(stats)
//              .withWeeklyStats(stats)
//              .withMonthlyStats(stats)
//              .withDate(RestUtil.DATE_FORMAT.format(LocalDate.now()));
//    }
//    return details;
//  }

    /**
     * Merge two sets of tags
     */
    public static void mergeTags(List<TagLabel> mergeTo, List<TagLabel> mergeFrom) {
        if (CommonUtil.nullOrEmpty(mergeFrom)) {
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

//    public static List<String> getJsonDataResources(String path) throws IOException {
//        return CommonUtil.getResources(Pattern.compile(path));
//    }

//    public static <T extends EntityInterface> List<String> toFQNs(List<T> entities) {
//        if (entities == null) {
//            return Collections.emptyList();
//        }
//        List<String> entityReferences = new ArrayList<>();
//        for (T entity : entities) {
//            entityReferences.add(entity.getFullyQualifiedName());
//        }
//        return entityReferences;
//    }

    public static List<UUID> strToIds(List<String> list) {
        return list.stream().map(UUID::fromString).collect(Collectors.toList());
    }

    public static List<EntityReference> toEntityReferences(List<UUID> ids, String entityType) {
        if (ids == null) {
            return null;
        }
        return ids.stream()
                .map(id -> new EntityReference().withId(id).withType(entityType))
                .collect(Collectors.toList());
    }

    public static List<UUID> refToIds(List<EntityReference> refs) {
        if (refs == null) {
            return null;
        }
        return refs.stream().map(EntityReference::getId).collect(Collectors.toList());
    }

    public static <T> boolean isDescriptionRequired(Class<T> clz) {
        // Returns true if description field in entity is required
        try {
            java.lang.reflect.Field description = clz.getDeclaredField(Entity.FIELD_DESCRIPTION);
            return description.getAnnotation(NotNull.class) != null;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * Entity version extension name formed by entityType.version.versionNumber. Example -
     * `table.version.0.1`
     */
    public static String getVersionExtension(String entityType, Double version) {
        return String.format("%s.%s", getVersionExtensionPrefix(entityType), version.toString());
    }

    /**
     * Entity version extension name prefix formed by `entityType.version`. Example - `table.version`
     */
    public static String getVersionExtensionPrefix(String entityType) {
        return String.format("%s.%s", entityType, "version");
    }

    public static Double getVersion(String extension) {
        String[] s = extension.split("\\.");
        String versionString = s[2] + "." + s[3];
        return Double.valueOf(versionString);
    }

//    public static String getLocalColumnName(String tableFqn, String columnFqn) {
//        // Return for fqn=service:database:table:c1 -> c1
//        // Return for fqn=service:database:table:c1:c2 -> c1:c2 (note different from just the local name
//        // of the column c2)
//        return columnFqn.replace(tableFqn + Entity.SEPARATOR, "");
//    }
//
//    public static String getFieldName(String... strings) {
//        return String.join(Entity.SEPARATOR, strings);
//    }

    /**
     * Return column field name of format "columns".columnName.columnFieldName
     */
//    public static String getColumnField(Column column, String columnField) {
//        // Remove table FQN from column FQN to get the local name
//        String localColumnName = column.getName();
//        return columnField == null
//                ? FullyQualifiedName.build("columns", localColumnName)
//                : FullyQualifiedName.build("columns", localColumnName, columnField);
//    }
//
//    /**
//     * Return schema field name of format "schemaFields".fieldName.fieldName
//     */
//    public static String getSchemaField(Topic topic, Field field, String fieldName) {
//        // Remove topic FQN from schemaField FQN to get the local name
//        String localFieldName =
//                EntityUtil.getLocalColumnName(topic.getFullyQualifiedName(), field.getFullyQualifiedName());
//        return fieldName == null
//                ? FullyQualifiedName.build("schemaFields", localFieldName)
//                : FullyQualifiedName.build("schemaFields", localFieldName, fieldName);
//    }
//
//    public static String getSchemaField(APIEndpoint apiEndpoint, Field field, String fieldName) {
//        // Remove APIEndpoint FQN from schemaField FQN to get the local name
//        String localFieldName =
//                EntityUtil.getLocalColumnName(
//                        apiEndpoint.getFullyQualifiedName(), field.getFullyQualifiedName());
//        return fieldName == null
//                ? FullyQualifiedName.build("schemaFields", localFieldName)
//                : FullyQualifiedName.build("schemaFields", localFieldName, fieldName);
//    }
//
//    /**
//     * Return searchIndex field name of format "fields".fieldName.fieldName
//     */
//    public static String getSearchIndexField(
//            SearchIndex searchIndex, SearchIndexField field, String fieldName) {
//        // Remove topic FQN from schemaField FQN to get the local name
//        String localFieldName =
//                EntityUtil.getLocalColumnName(
//                        searchIndex.getFullyQualifiedName(), field.getFullyQualifiedName());
//        return fieldName == null
//                ? FullyQualifiedName.build("fields", localFieldName)
//                : FullyQualifiedName.build("fields", localFieldName, fieldName);
//    }
//
//    /**
//     * Return extension field name of format "extension".fieldName
//     */
//    public static String getExtensionField(String key) {
//        return FullyQualifiedName.build("extension", key);
//    }
    public static Double previousVersion(Double version) {
        return Math.round((version - 0.1) * 10.0) / 10.0;
    }

    public static Double nextVersion(Double version) {
        return Math.round((version + 0.1) * 10.0) / 10.0;
    }

    public static Double nextMajorVersion(Double version) {
        return Math.round((version + 1.0) * 10.0) / 10.0;
    }

//    public static void copy(EntityReference from, EntityReference to) {
//        to.withType(from.getType())
//                .withId(from.getId())
//                .withName(from.getName())
//                .withDisplayName(from.getDisplayName())
//                .withFullyQualifiedName(from.getFullyQualifiedName())
//                .withDeleted(from.getDeleted());
//    }

    public static List<TagLabel> toTagLabels(GlossaryTerm... terms) {
        List<TagLabel> list = new ArrayList<>();
        for (GlossaryTerm term : terms) {
            list.add(toTagLabel(term));
        }
        return list;
    }

    public static List<TagLabel> toTagLabels(Tag... tags) {
        List<TagLabel> list = new ArrayList<>();
        for (Tag tag : tags) {
            list.add(toTagLabel(tag));
        }
        return list;
    }

    public static TagLabel toTagLabel(GlossaryTerm term) {
        return new TagLabel()
                .withId(term.getId())
                .withName(term.getName())
                .withDisplayName(term.getDisplayName())
                .withDescription(term.getDescription())
                .withSource(TagSource.GLOSSARY);
    }

    public static TagLabel toTagLabel(Tag tag) {
        return new TagLabel()
                .withId(tag.getId())
                .withName(tag.getName())
                .withDisplayName(tag.getDisplayName())
                .withDescription(tag.getDescription())
                .withSource(TagSource.CLASSIFICATION);
    }

//    public static UUID getId(EntityReference ref) {
//        return ref == null ? null : ref.getId();
//    }

//    public static EntityReference getEntityReference(EntityInterface entity) {
//        return entity == null ? null : entity.getEntityReference();
//    }
//
//    public static EntityReference getEntityReference(String entityType, String fqn) {
//        return fqn == null
//                ? null
//                : new EntityReference().withType(entityType).withFullyQualifiedName(fqn);
//    }
//
//    public static List<EntityReference> getEntityReferences(String entityType, List<String> fqns) {
//        if (CommonUtil.nullOrEmpty(fqns)) {
//            return null;
//        }
//        List<EntityReference> references = new ArrayList<>();
//        for (String fqn : fqns) {
//            references.add(Entity.getEntityReferenceByName(entityType, fqn, Include.NON_DELETED));
//        }
//        return references;
//    }
//
//    // Get EntityReference by ID, used in extension(for Page)
//    @SuppressWarnings("unused")
//    public static List<EntityReference> getEntityReferencesById(String entityType, List<UUID> ids) {
//        if (CommonUtil.nullOrEmpty(ids)) {
//            return null;
//        }
//        List<EntityReference> references = new ArrayList<>();
//        for (UUID id : ids) {
//            references.add(Entity.getEntityReferenceById(entityType, id, Include.NON_DELETED));
//        }
//        return references;
//    }
//
//    public static Column getColumn(Table table, String columnName) {
//        return table.getColumns().stream()
//                .filter(c -> c.getName().equals(columnName))
//                .findFirst()
//                .orElse(null);
//    }


    public static void validateProfileSample(String profileSampleType, double profileSampleValue) {
        if (profileSampleType.equals("PERCENTAGE")
                && (profileSampleValue < 0 || profileSampleValue > 100.0)) {
            throw new IllegalArgumentException("Profile sample value must be between 0 and 100");
        }
    }

//
//    public static Column findColumn(List<Column> columns, String columnName) {
//        return columns.stream()
//                .filter(c -> c.getName().equals(columnName))
//                .findFirst()
//                .orElseThrow(
//                        () ->
//                                new IllegalArgumentException(
//                                        CatalogExceptionMessage.invalidFieldName("column", columnName)));
//    }
//
//    public static <T extends FieldInterface> List<T> getFlattenedEntityField(List<T> fields) {
//        List<T> flattenedFields = new ArrayList<>();
//        fields.forEach(column -> flattenEntityField(column, flattenedFields));
//        return flattenedFields;
//    }

//    private static <T extends FieldInterface> void flattenEntityField(
//            T field, List<T> flattenedFields) {
//        flattenedFields.add(field);
//        List<T> children = (List<T>) field.getChildren();
//        for (T child : CommonUtil.listOrEmpty(children)) {
//            flattenEntityField(child, flattenedFields);
//        }
//    }
}
