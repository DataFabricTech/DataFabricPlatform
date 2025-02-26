package com.mobigen.vdap.server.models;

import com.mobigen.vdap.server.util.EntityUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EntityVersionPair {
    private final Double version;
    private final String entityJson;

    public EntityVersionPair(ExtensionRecord extensionRecord) {
        this.version = EntityUtil.getVersion(extensionRecord.extensionName());
        this.entityJson = extensionRecord.extensionJson();
    }

    public record ExtensionRecord(String extensionName, String extensionJson) {
    }

    public record ExtensionRecordWithId(UUID id, String extensionName, String extensionJson) {
    }
}

