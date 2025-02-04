package com.mobigen.datafabric.relationship.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataRelationshipResponse {
    private Integer id;
    private String name;
    private String creator;
    private String trainStatus;
    private String solutionType;
    private String integrationHistory;
    private String interactionData;
    private String metadataPath;
    private String createdAt;
    private String finishedAt;
}
