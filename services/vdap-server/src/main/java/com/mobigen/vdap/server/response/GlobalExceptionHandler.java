package com.mobigen.vdap.server.response;

import com.mobigen.vdap.server.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(BindException.class)
    public Object bindExceptionHandler(BindException e) {
        log.error("Bind Exception[ {} ]", e.getMessage());
        return e;
    }

    /**
     * 지원하지 않는 HTTP 메서드(예: GET, POST, PUT 등)를 요청할 경우 발생.
     */
    @ResponseBody
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object requestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("Request Method Not Supported Exception [ {} ]", e.getMessage());
        return e;
    }

    /**
     * 요청 본문을 읽을 수 없는 경우 발생.
     * 예: 잘못된 JSON 데이터를 요청으로 보냈을 때.
     */
    @ResponseBody
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("Request Message Not Readable Exception [ {} ]", e.getMessage());
        return e;
    }

    /**
     * 요청 파라미터가 누락된 경우 발생.
     * 예: @RequestParam("id")가 필수인데 요청에서 빠진 경우.
     */
    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object HttpMessageNotReadableException(MissingServletRequestParameterException e) {
        log.error("Request Parameter Missing Exception [ {} ]", e.getMessage());
        return e;
    }

    /**
     * CustomException 클래스로 서비스 로직에서의 예외를 처리한다.
     */
    @ResponseBody
    @ExceptionHandler(CustomException.class)
    public Object CustomExceptionHandler(CustomException e) {
        log.error("User(Service)Define Exception[ {} ]", e.getMessage(), e);
        return e;
    }

    /**
     * 핸들링하지 않은 예외를 상위클래스인 Exception 클래스로 핸들링한다.
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Object unknownExceptionHandler(Exception e) {
        log.error("Unknown Exception[ {} ]", e.getMessage(), e);
        return new CustomException(String.format("Unknown exception[%s]", e.getMessage()), e, null);
    }

    /*
     * 클라이언트가 요청한 Content-Type이 서버에서 지원하지 않는 경우 발생.
     * org.springframework.web.HttpMediaTypeNotSupportedException
     *
     * 서버가 응답할 수 있는 Content-Type이 클라이언트가 허용하는 타입과 맞지 않을 때 발생.
     * 예: 클라이언트가 Accept: application/xml을 요청했지만, 서버는 application/json만 지원하는 경우.
     * org.springframework.web.HttpMediaTypeNotAcceptableException
     *
     */

}
