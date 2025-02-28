package com.mobigen.monitoring.vo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class TableColumnInfo {
    private String name;
    private String dataType;
    private String dataLength;
    private String fullyQualifiedName;
    private String constraint;
}
