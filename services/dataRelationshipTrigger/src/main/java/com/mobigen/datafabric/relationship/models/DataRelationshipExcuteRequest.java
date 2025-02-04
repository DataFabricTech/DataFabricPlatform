package com.mobigen.datafabric.relationship.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DataRelationshipExcuteRequest {
    private String integrationHistory;
    private String interaction_data;
    private String metadata_path;
}
