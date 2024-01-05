package com.mobigen.datafabric.extraction.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "data_model")
public class DataModel {
    // TODO Share로 이동할 Class
    @Id
    @Nullable
    private String id;


    @Nullable
    private String name;
    private String description;

    private String type;
    private String format;

    private String status;


    @Nullable
    private Long createdAt;

    private Long lastModifiedAt;

    @OneToOne
    private DataLocation dataLocation;

    @OneToMany
    private List<DataMetadata> dataMetadata;

//    private User lastModifier;
//    private User createdBy;
//    private Permission permission;
//    private Category[] categories;
//    private Meta[] metas;
//    public String[] tags;
//
//    @Nullable
//    private User creator;
//
//    @Getter
//    @Setter
//    public static class Meta {
//        private boolean isSystem;
//        private String key;
//        private String value;
//    }
//
//    @Getter
//    @Setter
//    public static class Category {
//        private String id;
//        private String name;
//        private String description;
//        private String parentId;
//        private String path;
//    }
//
//    @Getter
//    @Setter
//    public static class Permission {
//        // todo remove default value
//        private boolean read = true;
//        private boolean write = true;
//    }
//
//    @Getter
//    @Setter
//    public static class User {
//        private String id;
//        private String name;
//        private String nickname;
//        private String phone;
//        private String email;
//    }
}
