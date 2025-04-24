package com.mobigen.monitoring.vo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class TableProfileVo {
    String name; // table name
    String fullyQualifiedName; // table fqn
    Long updatedAt;
    private Long columnCount;  // profile.columnCount;
    private Long rowCount; // profile.rowCount;
}
