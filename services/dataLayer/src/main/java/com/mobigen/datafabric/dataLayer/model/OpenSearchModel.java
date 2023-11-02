package com.mobigen.datafabric.dataLayer.model;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class OpenSearchModel {
    @Nullable
    private String id;
    @Nullable
    private String name;
    private String description;
    private String type;
    private Meta[] metas;
    private String format;
    private String knowledgeGraph;
    private boolean status;
    private String[] categories;
    private String[] tags;
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
