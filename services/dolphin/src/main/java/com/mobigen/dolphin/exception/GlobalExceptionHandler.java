package com.mobigen.dolphin.exception;

import com.mobigen.dolphin.dto.response.MessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
                .body(new MessageDto(e.getErrorCode().getStatus(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageDto(500, "처리 하지 못 한 내부 에러가 발생 했습니다. " + e.getMessage()));
    }
}
