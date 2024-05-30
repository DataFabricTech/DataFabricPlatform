package com.mobigen.dolphin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

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
}
