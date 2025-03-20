package com.mobigen.vdap.server.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class TagUsageId implements Serializable {
    private Integer source;
    private String sourceId;
    private String tagId;
    private String targetType;
    private String targetId;
}