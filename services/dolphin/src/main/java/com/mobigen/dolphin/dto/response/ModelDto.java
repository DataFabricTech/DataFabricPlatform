package com.mobigen.dolphin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
@Builder
public class ModelDto {
    private UUID id;
    private String name;
    private String description;
}
