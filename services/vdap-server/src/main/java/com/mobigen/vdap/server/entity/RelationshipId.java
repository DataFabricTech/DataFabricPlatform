package com.mobigen.vdap.server.entity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class RelationshipId implements Serializable {
    private String fromId;
    private String toId;
    private Integer relation;
} 