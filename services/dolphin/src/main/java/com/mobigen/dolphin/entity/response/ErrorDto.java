package com.mobigen.dolphin.entity.response;

import lombok.AllArgsConstructor;
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
public class ErrorDto {
    private int code;
    private String message;
}
