package com.mobigen.dolphin.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
@Setter
public class OMNotifyDto {
    private UUID id;
    @JsonProperty("eventType")
    private EventType eventType;
    private String entityType;
    private String entityId;
    private Float previousVersion;
    private Float currentVersion;
    private String userName;
    private Long timestamp;
    private String entity;

    @Getter
    @RequiredArgsConstructor
    public enum EventType {
        ENTITY_CREATED("entityCreated"),
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
            return UNSUPPORTED;
        }
    }
}
