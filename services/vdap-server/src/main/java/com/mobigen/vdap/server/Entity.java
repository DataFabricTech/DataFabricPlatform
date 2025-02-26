package com.mobigen.vdap.server;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public final class Entity {
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
    // Reserved names in Server
    //
    public static final String ADMIN_USER_NAME = "admin";

    /**
     * Get list of all the entity field names from JsonPropertyOrder annotation from generated java class from entity.json
     */
    public static <T> Set<String> getEntityFields(Class<T> clz) {
        JsonPropertyOrder propertyOrder = clz.getAnnotation(JsonPropertyOrder.class);
        return new HashSet<>(Arrays.asList(propertyOrder.value()));
    }
}
