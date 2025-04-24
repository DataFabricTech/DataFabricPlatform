package com.mobigen.monitoring.dto.response.fabric;

import com.fasterxml.jackson.databind.JsonNode;
import com.mobigen.monitoring.vo.ColumnInfos;
import com.mobigen.monitoring.vo.TableInfo;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static com.mobigen.monitoring.enums.OpenMetadataEnum.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class TableInfoResponseDto {
    private String fullyQualifiedName;
    private Long updatedAt;
    private List<ColumnInfo> columns;
    private String databaseSchema; // databaseSchema.name
    private String database; // database.name
    private Boolean deleted;

    public static TableInfoResponseDto of(JsonNode tableInfoJsonNode) {
        List<ColumnInfo> columns = new ArrayList<>();

        for (JsonNode columnInfo : tableInfoJsonNode.get(COLUMNS.getName())) {
            columns.add(
                    ColumnInfo.builder()
                            .name(columnInfo.get(NAME.getName()).asText())
                            .build()
            );
        }

        return TableInfoResponseDto.builder()
                .fullyQualifiedName(tableInfoJsonNode.get(FULLY_QUALIFIED_NAME.getName()).asText()) // b-iris.default.container_management.containers
                .updatedAt(Long.valueOf(tableInfoJsonNode.get(UPDATED_AT.getName()).toString()))
                .columns(columns)
                .databaseSchema(tableInfoJsonNode.get(DATABASE_SCHEMA.getName()).asText()) // databaseSchema.name
                .database(tableInfoJsonNode.get(DATABASE.getName()).asText()) // database.name
                .deleted(tableInfoJsonNode.get(DELETED.getName()).asBoolean())
                .build();
    }
}
