package com.mobigen.dolphin.exception;

import com.mobigen.dolphin.entity.response.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SqlParseException.class)
    protected ResponseEntity<?> handleSqlParseException(SqlParseException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(new ErrorDto(e.getErrorCode().getStatus(), e.getMessage()));
    }
}
