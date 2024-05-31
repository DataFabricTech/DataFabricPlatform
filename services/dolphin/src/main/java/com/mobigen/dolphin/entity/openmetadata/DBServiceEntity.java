package com.mobigen.dolphin.entity.openmetadata;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Data
public class DBServiceEntity {
    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;
    private String name;
    private String fullyQualifiedName;
    private String displayName;
    private String serviceType;
    private String description;
    private ConnectionEntity connection;
    private Float version;
    private Long updatedAt;
    private String updatedBy;
    private String href;
    private ChangeDescriptionEntity changeDescription;
    private Boolean deleted;

    @Data
    public static class ConnectionEntity {
        private ConfigEntity config;

        @Data
        public static class ConfigEntity {
            private String type;
            private String scheme;
            private String username;
            private String password;
            private String hostPort;
            private String databaseName;
            private Boolean supportsMetadataExtraction;
            private Boolean supportsDBTExtraction;
            private Boolean supportsProfiler;
            private Boolean supportsQueryComment;
        }
    }

    @Data
    public static class ChangeDescriptionEntity {
        private List<String> fieldsAdded;
        private List<FieldsUpdatedEntity> fieldsUpdated;
        private List<String> fieldsDeleted;
        private Float previousVersion;

        @Data
        public static class FieldsUpdatedEntity {
            private String name;
            private Boolean oldValue;
            private Boolean newValue;
        }
    }
}
