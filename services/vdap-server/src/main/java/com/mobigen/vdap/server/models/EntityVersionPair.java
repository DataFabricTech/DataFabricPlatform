package com.mobigen.vdap.server.models;

import com.mobigen.vdap.server.entity.EntityExtension;
import com.mobigen.vdap.server.util.EntityUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EntityVersionPair {
    private final Double version;
    private final String entityJson;

    public EntityVersionPair(EntityExtension extension) {
        this.version = EntityUtil.getVersion(extension.getExtension());
        this.entityJson = extension.getJson();
    }
}

