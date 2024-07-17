package com.mobigen.dolphin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@AllArgsConstructor
@Builder
public class MessageDto {
    private int code;
    private String message;
}
