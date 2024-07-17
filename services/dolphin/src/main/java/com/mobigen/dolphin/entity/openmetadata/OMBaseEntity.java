package com.mobigen.dolphin.entity.openmetadata;

import lombok.Data;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Data
public class OMBaseEntity {
    private UUID id;
    private EntityType type;
    private String name;
    private String fullyQualifiedName;
    private String displayName;
    private Boolean deleted;
    private String href;
}
