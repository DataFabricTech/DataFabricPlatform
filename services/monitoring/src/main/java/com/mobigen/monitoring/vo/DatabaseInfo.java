package com.mobigen.monitoring.vo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DatabaseInfo {
    private String id;
    private String type;
    private String name;
    private String fullyQualifiedName;
    private Boolean deleted;
}
