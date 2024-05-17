package com.mobigen.dolphin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@AllArgsConstructor
@Getter
public enum ErrorCode {
    INVALID_SQL(400, "잘 못 된 SQL 입니다.");
    private final int status;
    private final String message;
}
