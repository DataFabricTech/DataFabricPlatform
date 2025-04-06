package com.mobigen.monitoring.enums;

import com.mobigen.monitoring.exception.CustomException;

public enum ServiceModelType {
    DATABASE_SERVICE,
    STORAGE_SERVICE;

    public static ServiceModelType getServiceModelType(String type) {
        for (ServiceModelType serviceModelType : ServiceModelType.values()) {
            if (serviceModelType.name().equalsIgnoreCase(type)) {
                return serviceModelType;
            }
        }

        throw new CustomException("Type is invalid");
    }
}
