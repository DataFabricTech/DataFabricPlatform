package com.mobigen.datafabric.dataLayer.model;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class DataModel {
    @Nullable
    private String id;

    @Nullable
    private String name;
    private String description;

    private String type;
    private String format;

    private String status;

    private Category[] categories;
    private Meta[] metas;
    public String[] tags;

    @Nullable
    private User creator;
    @Nullable
    private Long createdAt;

    private User lastModifier;
    private Long lastModifiedAt;

    private Permission permission;

    @Getter
    @Setter
    public static class Meta {
        private boolean isSystem;
        private String key;
        private String value;
    }

    @Getter
    @Setter
    public static class Category {
        private String id;
        private String name;
        private String description;
        private String parentId;
        private String path;
    }

    @Getter
    @Setter
    public static class Permission {
        // todo remove default value
        private boolean read = true;
        private boolean write = true;
    }

    @Getter
    @Setter
    public static class User {
        private String id;
        private String name;
        private String nickname;
        private String phone;
        private String email;
    }
}
