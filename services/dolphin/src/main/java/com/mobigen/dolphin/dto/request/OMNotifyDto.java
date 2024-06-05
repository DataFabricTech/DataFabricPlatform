package com.mobigen.dolphin.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobigen.dolphin.entity.openmetadata.EntityType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@Getter
@Setter
public class OMNotifyDto {
    private UUID id;
    @JsonProperty("eventType")
    private EventType eventType;
    private EntityType entityType;
    private UUID entityId;
    private Float previousVersion;
    private Float currentVersion;
    private String userName;
    private Long timestamp;
    private String entity;

    @Getter
    @RequiredArgsConstructor
    public enum EventType {
        ENTITY_CREATED("entityCreated"),
        ENTITY_UPDATED("entityUpdated"),
        ENTITY_SOFT_DELETED("entitySoftDeleted"),
        ENTITY_RESTORED("entityRestored"),
        ENTITY_DELETED("entityDeleted"),
        UNSUPPORTED("unsupported"),
        ;

        private final String value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static EventType from(String value) {
            for (EventType type : EventType.values()) {
                if (type.getValue().equals(value)) {
                    return type;
                }
            }
            log.warn("Unsupported event type: {}", value);
            return UNSUPPORTED;
        }
    }
}
