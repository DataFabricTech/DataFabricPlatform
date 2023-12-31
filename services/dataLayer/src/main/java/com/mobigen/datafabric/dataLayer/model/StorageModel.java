package com.mobigen.datafabric.dataLayer.model;

import com.mobigen.datafabric.share.protobuf.Utilities;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class StorageModel {
    @Nullable
    private String id;

    @Nullable
    private String name;
    private String desc;
    private String[] tag;
    private String createdBy;
    private String storageTypeName;
    private long updatedAt;
    private Utilities.Status status;
}
