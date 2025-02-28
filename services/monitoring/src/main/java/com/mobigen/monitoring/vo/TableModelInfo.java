package com.mobigen.monitoring.vo;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class TableModelInfo {
    private String id;
    private String name;
    private String fullyQualifiedName;
    private String description;
    private Long updatedAt;
    private String updatedAyBy;
    private List<TableColumnInfo> columns;
    private String serviceType;
    private DatabaseInfo database;
    private DatabaseSchemaInfo databaseSchema;
    private Boolean deleted;
    private Integer total;
}
