package com.mobigen.vdap.server;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.TreeSet;

@Slf4j
public final class Entity {
    // List of all the entities
    private static final Set<String> ENTITY_LIST = new TreeSet<>();

    // Common field names
    public static final String FIELD_OWNERS = "owners";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_FOLLOWERS = "followers";
    public static final String FIELD_VOTES = "votes";
    public static final String FIELD_TAGS = "tags";
    public static final String FIELD_DELETED = "deleted";
    public static final String FIELD_PIPELINE_STATUS = "pipelineStatus";
    public static final String FIELD_DISPLAY_NAME = "displayName";
    public static final String FIELD_EXTENSION = "extension";
    public static final String FIELD_USAGE_SUMMARY = "usageSummary";
    public static final String FIELD_CHILDREN = "children";
    public static final String FIELD_PARENT = "parent";
    public static final String FIELD_REVIEWERS = "reviewers";
    public static final String FIELD_EXPERTS = "experts";
    public static final String FIELD_ASSETS = "assets";

    public static final String FIELD_LIFE_CYCLE = "lifeCycle";
    public static final String FIELD_CERTIFICATION = "certification";

    public static final String FIELD_DISABLED = "disabled";

    public static final String FIELD_TEST_SUITES = "testSuites";

    public static final String FIELD_RELATED_TERMS = "relatedTerms";

    //
    // Service entities
    //
//    public static final String DATABASE_SERVICE = "databaseService";
//    public static final String SEARCH_SERVICE = "searchService";
//    public static final String API_SERVICE = "apiService";
    public static final String STORAGE_SERVICE = "storageService";
    //
    // Data asset entities
    //
    public static final String DATABASE = "database";
    public static final String DATABASE_SCHEMA = "databaseSchema";
    public static final String TABLE = "table";
    //  public static final String PIPELINE = "pipeline";
//  public static final String TASK = "task";
    public static final String SEARCH_INDEX = "searchIndex";

    public static final String API_COLLCECTION = "apiCollection";
    public static final String API_ENDPOINT = "apiEndpoint";

    public static final String API = "api";

    public static final String BUCKET = "bucket";
    public static final String FOLDER = "folder";
    public static final String OBJECT = "object";
    public static final String QUERY = "query";

    public static final String GLOSSARY = "glossary";
    public static final String GLOSSARY_TERM = "glossaryTerm";
    public static final String TAG = "tag";
    public static final String CLASSIFICATION = "classification";
    public static final String TYPE = "type";
    public static final String TEST_DEFINITION = "testDefinition";
    public static final String TEST_CONNECTION_DEFINITION = "testConnectionDefinition";
    public static final String TEST_SUITE = "testSuite";
    public static final String TEST_CASE = "testCase";
    public static final String PAGE = "page";

    //
    // Operation related entities
    //
    public static final String INGESTION_PIPELINE = "ingestionPipeline";

    //
    // Reserved names in OpenMetadata
    //
    public static final String ADMIN_USER_NAME = "admin";

    // ServiceType - Service Entity name map
//    static final Map<ServiceType, String> SERVICE_TYPE_ENTITY_MAP = new EnumMap<>(ServiceType.class);
//    // entity type to service entity name map
//    static final Map<String, String> ENTITY_SERVICE_TYPE_MAP = new HashMap<>();
//    public static final List<String> PARENT_ENTITY_TYPES = new ArrayList<>();
//
//    static {
////        SERVICE_TYPE_ENTITY_MAP.put(ServiceType.DATABASE, DATABASE_SERVICE);
//        SERVICE_TYPE_ENTITY_MAP.put(ServiceType.STORAGE, STORAGE_SERVICE);

    /// /        SERVICE_TYPE_ENTITY_MAP.put(ServiceType.SEARCH, SEARCH_SERVICE);
    /// /        SERVICE_TYPE_ENTITY_MAP.put(ServiceType.API, API_SERVICE);
//
//        ENTITY_SERVICE_TYPE_MAP.put(DATABASE, DATABASE_SERVICE);
//        ENTITY_SERVICE_TYPE_MAP.put(DATABASE_SCHEMA, DATABASE_SERVICE);
//        ENTITY_SERVICE_TYPE_MAP.put(TABLE, DATABASE_SERVICE);
//        ENTITY_SERVICE_TYPE_MAP.put(QUERY, DATABASE_SERVICE);
//        ENTITY_SERVICE_TYPE_MAP.put(API, API_SERVICE);
//        ENTITY_SERVICE_TYPE_MAP.put(API_COLLCECTION, API_SERVICE);
//        ENTITY_SERVICE_TYPE_MAP.put(API_ENDPOINT, API_SERVICE);
//        ENTITY_SERVICE_TYPE_MAP.put(BUCKET, STORAGE_SERVICE);
//        ENTITY_SERVICE_TYPE_MAP.put(FOLDER, STORAGE_SERVICE);
//        ENTITY_SERVICE_TYPE_MAP.put(OBJECT, STORAGE_SERVICE);
//        ENTITY_SERVICE_TYPE_MAP.put(SEARCH_INDEX, SEARCH_SERVICE);
//
//        PARENT_ENTITY_TYPES.addAll(
//                listOf(
//                        DATABASE_SERVICE,
//                        API_SERVICE,
//                        API_COLLCECTION,
//                        STORAGE_SERVICE,
//                        SEARCH_SERVICE,
//                        DATABASE,
//                        DATABASE_SCHEMA,
//                        CLASSIFICATION,
//                        GLOSSARY,
//                        TEST_SUITE
//                ));
//    }
    private Entity() {
    }

  /*
  public static <T extends EntityTimeSeriesInterface> void registerEntity(
      Class<T> clazz, String entity, EntityTimeSeriesRepository<T> entityRepository) {
    ENTITY_TS_REPOSITORY_MAP.put(entity, entityRepository);
    EntityTimeSeriesInterface.CANONICAL_ENTITY_NAME_MAP.put(
        entity.toLowerCase(Locale.ROOT), entity);
    EntityTimeSeriesInterface.ENTITY_TYPE_TO_CLASS_MAP.put(entity.toLowerCase(Locale.ROOT), clazz);
    ENTITY_LIST.add(entity);

    LOG.debug("Registering entity time series {} {}", clazz, entity);
  }

  public static void registerResourcePermissions(
      String entity, List<MetadataOperation> entitySpecificOperations) {
    // Set up entity operations for permissions
    Class<?> clazz = getEntityClassFromType(entity);
    ResourceRegistry.addResource(entity, entitySpecificOperations, getEntityFields(clazz));
  }

  public static void registerTimeSeriesResourcePermissions(String entity) {
    // Set up entity operations for permissions
    Class<?> clazz = getEntityTimeSeriesClassFromType(entity);
    ResourceRegistry.addResource(entity, null, getEntityFields(clazz));
  }

  public static Set<String> getEntityList() {
    return Collections.unmodifiableSet(ENTITY_LIST);
  }

  public static EntityReference getEntityReference(EntityReference ref, Include include) {
    if (ref == null) {
      return null;
    }
    return ref.getId() != null
        ? getEntityReferenceById(ref.getType(), ref.getId(), include)
        : getEntityReferenceByName(ref.getType(), ref.getFullyQualifiedName(), include);
  }

  public static EntityReference getEntityReferenceById(
      @NonNull String entityType, @NonNull UUID id, Include include) {
    EntityRepository<? extends EntityInterface> repository = getEntityRepository(entityType);
    include = repository.supportsSoftDelete ? Include.ALL : include;
    return repository.getReference(id, include);
  }

  public static EntityReference getEntityReferenceByName(
      @NonNull String entityType, String fqn, Include include) {
    if (fqn == null) {
      return null;
    }
    EntityRepository<? extends EntityInterface> repository = getEntityRepository(entityType);
    return repository.getReferenceByName(fqn, include);
  }

  public static List<EntityReference> getOwners(@NonNull EntityReference reference) {
    EntityRepository<? extends EntityInterface> repository =
        getEntityRepository(reference.getType());
    return repository.getOwners(reference);
  }

  public static void withHref(UriInfo uriInfo, List<EntityReference> list) {
    listOrEmpty(list).forEach(ref -> withHref(uriInfo, ref));
  }

  public static void withHref(UriInfo uriInfo, EntityReference ref) {
    if (ref == null) {
      return;
    }
    String entityType = ref.getType();
    EntityRepository<?> entityRepository = getEntityRepository(entityType);
    URI href = entityRepository.getHref(uriInfo, ref.getId());
    ref.withHref(href);
  }

  public static Fields getFields(String entityType, List<String> fields) {
    EntityRepository<?> entityRepository = Entity.getEntityRepository(entityType);
    return entityRepository.getFields(String.join(",", fields));
  }

  public static <T> T getEntity(EntityReference ref, String fields, Include include) {
    return ref.getId() != null
        ? getEntity(ref.getType(), ref.getId(), fields, include)
        : getEntityByName(ref.getType(), ref.getFullyQualifiedName(), fields, include);
  }

  public static <T> T getEntityOrNull(
      EntityReference entityReference, String field, Include include) {
    if (entityReference == null) return null;
    return Entity.getEntity(entityReference, field, include);
  }

  public static <T> T getEntity(EntityLink link, String fields, Include include) {
    return getEntityByName(link.getEntityType(), link.getEntityFQN(), fields, include);

  }
  */

//  /** Retrieve the entity using id from given entity reference and fields */
//  public static <T> T getEntity(
//      String entityType, UUID id, String fields, Include include, boolean fromCache) {
//    EntityRepository<?> entityRepository = Entity.getEntityRepository(entityType);
//    @SuppressWarnings("unchecked")
//    T entity =
//        (T) entityRepository.get(null, id, entityRepository.getFields(fields), include, fromCache);
//    return entity;
//  }
//
//  public static <T> T getEntity(String entityType, UUID id, String fields, Include include) {
//    return getEntity(entityType, id, fields, include, true);
//  }
//
//  /** Retrieve the entity using id from given entity reference and fields */
//  public static <T> T getEntityByName(
//      String entityType, String fqn, String fields, Include include, boolean fromCache) {
//    EntityRepository<?> entityRepository = Entity.getEntityRepository(entityType);
//    @SuppressWarnings("unchecked")
//    T entity =
//        (T)
//            entityRepository.getByName(
//                null, fqn, entityRepository.getFields(fields), include, fromCache);
//    return entity;
//  }
//
//  public static <T> T getEntityByName(
//      String entityType, String fqn, String fields, Include include) {
//    return getEntityByName(entityType, fqn, fields, include, true);
//  }
//
//  /** Retrieve the corresponding entity repository for a given entity name. */
//  public static EntityRepository<? extends EntityInterface> getEntityRepository(
//      @NonNull String entityType) {
//    EntityRepository<? extends EntityInterface> entityRepository =
//        ENTITY_REPOSITORY_MAP.get(entityType);
//    if (entityRepository == null) {
//      throw EntityNotFoundException.byMessage(
//          CatalogExceptionMessage.entityRepositoryNotFound(entityType));
//    }
//    return entityRepository;
//  }
//
//  public static EntityTimeSeriesRepository<? extends EntityTimeSeriesInterface>
//      getEntityTimeSeriesRepository(@NonNull String entityType) {
//    EntityTimeSeriesRepository<? extends EntityTimeSeriesInterface> entityTimeSeriesRepository =
//        ENTITY_TS_REPOSITORY_MAP.get(entityType);
//    if (entityTimeSeriesRepository == null) {
//      throw EntityNotFoundException.byMessage(
//          CatalogExceptionMessage.entityTypeNotFound(entityType));
//    }
//    return entityTimeSeriesRepository;
//  }
//
//  /** Retrieve the corresponding entity repository for a given entity name. */
//  public static EntityRepository<? extends EntityInterface> getServiceEntityRepository(
//      @NonNull ServiceType serviceType) {
//    EntityRepository<? extends EntityInterface> entityRepository =
//        ENTITY_REPOSITORY_MAP.get(SERVICE_TYPE_ENTITY_MAP.get(serviceType));
//    if (entityRepository == null) {
//      throw EntityNotFoundException.byMessage(
//          CatalogExceptionMessage.entityTypeNotFound(serviceType.value()));
//    }
//    return entityRepository;
//  }
//
//  public static List<TagLabel> getEntityTags(String entityType, EntityInterface entity) {
//    EntityRepository<? extends EntityInterface> entityRepository = getEntityRepository(entityType);
//    return listOrEmpty(entityRepository.getAllTags(entity));
//  }
//
//  public static void deleteEntity(
//      String updatedBy, String entityType, UUID entityId, boolean recursive, boolean hardDelete) {
//    EntityRepository<?> dao = getEntityRepository(entityType);
//    try {
//      dao.find(entityId, Include.ALL);
//      dao.delete(updatedBy, entityId, recursive, hardDelete);
//    } catch (EntityNotFoundException e) {
//      LOG.warn("Entity {} is already deleted.", entityId);
//    }
//  }
//
//  public static void restoreEntity(String updatedBy, String entityType, UUID entityId) {
//    EntityRepository<?> dao = getEntityRepository(entityType);
//    dao.restoreEntity(updatedBy, entityType, entityId);
//  }
//
//  public static <T> String getEntityTypeFromClass(Class<T> clz) {
//    return EntityInterface.CANONICAL_ENTITY_NAME_MAP.get(
//        clz.getSimpleName().toLowerCase(Locale.ROOT));
//  }
//
//  public static String getEntityTypeFromObject(Object object) {
//    return EntityInterface.CANONICAL_ENTITY_NAME_MAP.get(
//        object.getClass().getSimpleName().toLowerCase(Locale.ROOT));
//  }
//
//  public static Class<? extends EntityInterface> getEntityClassFromType(String entityType) {
//    return EntityInterface.ENTITY_TYPE_TO_CLASS_MAP.get(entityType.toLowerCase(Locale.ROOT));
//  }
//
//  public static Class<? extends EntityTimeSeriesInterface> getEntityTimeSeriesClassFromType(
//      String entityType) {
//    return EntityTimeSeriesInterface.ENTITY_TYPE_TO_CLASS_MAP.get(
//        entityType.toLowerCase(Locale.ROOT));
//  }
//
//  /**
//   * Get list of all the entity field names from JsonPropertyOrder annotation from generated java class from entity.json
//   */
//  public static <T> Set<String> getEntityFields(Class<T> clz) {
//    JsonPropertyOrder propertyOrder = clz.getAnnotation(JsonPropertyOrder.class);
//    return new HashSet<>(Arrays.asList(propertyOrder.value()));
//  }
//
//  /** Returns true if the entity supports activity feeds, announcement, and tasks */
//  public static boolean supportsFeed(String entityType) {
//    return listOf(
//            TABLE,
//            DATABASE,
//            DATABASE_SCHEMA,
//            METRIC,
//            DASHBOARD,
//            DASHBOARD_DATA_MODEL,
//            PIPELINE,
//            CHART,
//            REPORT,
//            TOPIC,
//            MLMODEL,
//            CONTAINER,
//            QUERY,
//            GLOSSARY,
//            GLOSSARY_TERM,
//            TAG,
//            CLASSIFICATION)
//        .contains(entityType);
//  }
//
//  /** Class for getting validated entity list from a queryParam with list of entities. */
//  public static class EntityList {
//    private EntityList() {}
//
//    public static List<String> getEntityList(String name, String entitiesParam) {
//      if (entitiesParam == null) {
//        return Collections.emptyList();
//      }
//      entitiesParam = entitiesParam.replace(" ", "");
//      if (entitiesParam.equals("*")) {
//        return List.of("*");
//      }
//      List<String> list = Arrays.asList(entitiesParam.split(","));
//      validateEntities(name, list);
//      return list;
//    }
//
//    private static void validateEntities(String name, List<String> list) {
//      for (String entity : list) {
//        if (ENTITY_REPOSITORY_MAP.get(entity) == null) {
//          throw new IllegalArgumentException(
//              String.format("Invalid entity %s in query param %s", entity, name));
//        }
//      }
//    }
//  }
//
//  /** Compile a list of REST collections based on Resource classes marked with {@code Repository} annotation */
//  private static List<Class<?>> getRepositories() {
//    try (ScanResult scanResult =
//        new ClassGraph()
//            .enableAnnotationInfo()
//            .acceptPackages(PACKAGES.toArray(new String[0]))
//            .scan()) {
//      ClassInfoList classList = scanResult.getClassesWithAnnotation(Repository.class);
//      return classList.loadClasses();
//    }
//  }
//
//  public static <T extends FieldInterface> void populateEntityFieldTags(
//      String entityType, List<T> fields, String fqnPrefix, boolean setTags) {
//    EntityRepository<?> repository = Entity.getEntityRepository(entityType);
//    // Get Flattened Fields
//    List<T> flattenedFields = getFlattenedEntityField(fields);
//
//    // Fetch All tags belonging to Prefix
//    Map<String, List<TagLabel>> allTags = repository.getTagsByPrefix(fqnPrefix, ".%");
//    for (T c : listOrEmpty(flattenedFields)) {
//      if (setTags) {
//        List<TagLabel> columnTag =
//            allTags.get(FullyQualifiedName.buildHash(c.getFullyQualifiedName()));
//        if (columnTag == null) {
//          c.setTags(new ArrayList<>());
//        } else {
//          c.setTags(addDerivedTags(columnTag));
//        }
//      } else {
//        c.setTags(c.getTags());
//      }
//    }
//  }
//
//  public static SearchIndex buildSearchIndex(String entityType, Object entity) {
//    if (searchRepository != null) {
//      return searchRepository.getSearchIndexFactory().buildIndex(entityType, entity);
//    }
//    throw new BadRequestException("searchrepository not initialized");
//  }
//
//  public static <T> T getDao() {
//    return (T) collectionDAO;
//  }
//
//  public static <T> T getSearchRepo() {
//    return (T) searchRepository;
//  }
//
//  public static String getServiceType(String entityType) {
//    if (ENTITY_SERVICE_TYPE_MAP.containsKey(entityType)) {
//      return ENTITY_SERVICE_TYPE_MAP.get(entityType);
//    }
//    return entityType;
//  }
//
}
