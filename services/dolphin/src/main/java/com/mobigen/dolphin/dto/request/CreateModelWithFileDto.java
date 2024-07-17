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
public class CreateModelWithFileDto {
    @Schema(description = "DataModel name to create", example = "model_test_1")
    private String modelName;
    @Schema(description = "comment")
    private String comment;
    @Schema(description = "path of file")
    private String path;
    @Schema(description = "Id of file")
    private String fileId;
}
