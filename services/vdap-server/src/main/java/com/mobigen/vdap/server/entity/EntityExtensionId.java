package com.mobigen.vdap.server.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class EntityExtensionId implements Serializable {
    private String id;
    private String extension;
}