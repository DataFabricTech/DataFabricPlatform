package com.mobigen.monitoring.vo;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TableSchemaInfo {
    private String columnName;
    private String dataType;
    private String isNullable;
    private String columnDefault;
}
