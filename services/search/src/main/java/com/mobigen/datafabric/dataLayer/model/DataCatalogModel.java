package com.mobigen.datafabric.dataLayer.model;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class DataCatalogModel {
    @Nullable
    private String id;

    @Nullable
    private String name;
    private String description;
    private boolean status;

    private String dataType;
    private String dataFormat;

    private Meta[] userMeta;
    private Meta[] systemMeta;
    private String[] categories;
    private String[] tags;
    private String knowledgeGraph;
    private String connectorType;
    private String connectorName;
    @Nullable
    private String creatorId;
    @Nullable
    private String creatorName;
    private Long createdAt;

    @Getter
    @Setter
    public static class Meta {
        private String key;
        private String value;
    }
}
