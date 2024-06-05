package com.mobigen.dolphin.entity.openmetadata;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */

@Data
public class OMChangeDescriptionEntity {
    private List<String> fieldsAdded;
    private List<FieldsUpdatedEntity> fieldsUpdated;
    private List<String> fieldsDeleted;
    private Float previousVersion;

    @Data
    public static class FieldsUpdatedEntity {
        private String name;
        private Boolean oldValue;
        private Boolean newValue;
    }
}
