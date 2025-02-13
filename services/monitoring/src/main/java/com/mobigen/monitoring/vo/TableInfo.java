package com.mobigen.monitoring.vo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class TableInfo {
    private String tableName;
    private Long rows;
    private Long tableSize;
}
