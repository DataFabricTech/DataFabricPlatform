package com.mobigen.monitoring.enums;

import com.mobigen.monitoring.exception.CustomException;
import lombok.Getter;

@Getter
public enum ServiceEventEnum {
    CREATED,
    UPDATED,
    DELETED;

    public static ServiceEventEnum get(final String event) {
        for (ServiceEventEnum value : ServiceEventEnum.values()) {
            if (value.name().equalsIgnoreCase(event)) {
                return value;
            }
        }

        throw new CustomException(String.format("Invalid event name: %s", event));
    }
}
