package com.mobigen.dolphin.entity.openmetadata;

import lombok.Data;

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
public class OMTableEntity {
    private UUID id;
    private String name;
    private String fullyQualifiedName;
    private String description;
    private Float version;
    private Long updatedAt;
    private String updatedBy;
    private String href;
    private String tableType;
    private List<OMColumn> columns;
    private OMBaseEntity databaseSchema;
    private OMBaseEntity database;
    private OMBaseEntity service;
    private String serviceType;
    private OMChangeDescriptionEntity changeDescription;
    private Boolean deleted;
    private String sourceHash;

    @Data
    public static class OMColumn {
        private String name;
        private String dataType;
        private Integer dataLength;
        private String dataTypeDisplay;
        private String fullyQualifiedName;
    }
}
