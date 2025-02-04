package com.mobigen.datafabric.relationship.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openmetadata.schema.type.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metadata {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("name")
    private @Pattern(regexp = "^((?!::).)*$")
    @Size(min = 1, max = 256) String name;
    @JsonProperty("displayName")
    @JsonPropertyDescription("Display Name that identifies this table. " +
            "It could be title or label from the source services.")
    private String displayName;
    @JsonProperty("description")
    @JsonPropertyDescription("Text in Markdown format.")
    private String description;
    @JsonProperty("updatedAt")
    @JsonPropertyDescription("Timestamp in Unix epoch time milliseconds.")
    private Long updatedAt;
    @JsonProperty("updatedBy")
    @JsonPropertyDescription("User who made the update.")
    private String updatedBy;
    @JsonProperty("dataType")
    @JsonPropertyDescription("Data Type : Structured, Unstructured, Semi-Structured")
    private String dataType;
    @JsonProperty("tableType")
    @JsonPropertyDescription("Table Type : Regular, View, Fusion")
    private String tableType;
    @JsonProperty("fileFormat")
    @JsonPropertyDescription("File format in case of file.")
    private String fileFormat;
    @JsonProperty("fullPath")
    @JsonPropertyDescription("Full path of the container/file.")
    private String fullPath;
    @JsonProperty("size")
    @JsonPropertyDescription("The total size in KB this container has.")
    private Double size = null;
    @JsonProperty("columns")
    @JsonPropertyDescription("Columns in this table.")
    private List<Column> columns = null;
    @JsonProperty("tableConstraints")
    @JsonPropertyDescription("Table constraints.")
    private List<TableConstraint> tableConstraints = null;
    @JsonProperty("owner")
    private UUID owner;
    @JsonProperty("databaseSchema")
    @JsonPropertyDescription("This schema defines the EntityReference type used for referencing an entity. EntityReference is used for capturing relationships from one entity to another. For example, a table has an attribute called database of type EntityReference that captures the relationship of a table `belongs to a` database.")
    private EntityReference databaseSchema;
    @JsonProperty("database")
    @JsonPropertyDescription("This schema defines the EntityReference type used for referencing an entity. EntityReference is used for capturing relationships from one entity to another. For example, a table has an attribute called database of type EntityReference that captures the relationship of a table `belongs to a` database.")
    private EntityReference database;
    @JsonProperty("service")
    @JsonPropertyDescription("This schema defines the EntityReference type used for referencing an entity. EntityReference is used for capturing relationships from one entity to another. For example, a table has an attribute called database of type EntityReference that captures the relationship of a table `belongs to a` database.")
    private EntityReference service;
    @JsonProperty("serviceType")
    @JsonPropertyDescription("Type of database service such as MySQL, Postgres, MinIO, ...")
    private String serviceType;
    @JsonProperty("schemaDefinition")
    @JsonPropertyDescription("SQL query statement. Example - 'select * from orders'.")
    private String schemaDefinition;
    @JsonProperty("tags")
    @JsonPropertyDescription("Tags for this table.")
    private List<TagLabel> tags = null;
    @JsonProperty("followers")
    @JsonPropertyDescription("This schema defines the EntityReferenceList type used for referencing an entity. EntityReference is used for capturing relationships from one entity to another. For example, a table has an attribute called database of type EntityReference that captures the relationship of a table `belongs to a` database.")
    private List<EntityReference> followers = null;
    @JsonProperty("votes")
    @JsonPropertyDescription("This schema defines the Votes for a Data Asset.")
    private Votes votes;
    @JsonProperty("profile")
    @JsonPropertyDescription("This schema defines the type to capture the table's data profile.")
    private TableProfile profile;
    @JsonProperty("sampleData")
    @JsonPropertyDescription("This schema defines the type to capture rows of sample data for a table.")
    private Object sampleData;
    @JsonProperty("lineage")
    @JsonPropertyDescription("This data up/down lineage.")
    private EntityLineage lineage;
}
