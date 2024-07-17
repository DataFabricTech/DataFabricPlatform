package com.mobigen.dolphin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
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
public class ExecuteDto {
    @Schema(description = "Sql select query using DataModel", example = "select * from model_test_1")
    private String query;
    private List<ReferenceModel> referenceModels = new ArrayList<>();

    @Getter
    @Setter
    public static class ReferenceModel {
        private UUID id;
        private String name;
        private String fullyQualifiedName;
    }
}
