package com.mobigen.monitoring.vo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DatabaseSchemaInfo {
    private String id;
    private String type;
    private String name;
    private String fullyQualifiedName;
    private String displayName;
    private Boolean deleted;
    private Long total;
}
