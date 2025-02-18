package com.mobigen.vdap.server.response;

import com.mobigen.vdap.server.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(BindException.class)
    public Object bindExceptionHandler( BindException e ) {
        log.error( "Bind Exception[ {} ]", e.getMessage());
        return e;
    }

    @ResponseBody
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object requestMethodNotSupportedException( HttpRequestMethodNotSupportedException e ) {
        log.error( "Request Method Not Supported Exception [ {} ]", e.getMessage() );
//        Sentry.captureException( e );
        return e;
    }

    /**
     * @param e service define exception
     * @return Exception
     * CustomException 클래스로 서비스 로직에서의 예외를 처리한다.
     */
    @ResponseBody
    @ExceptionHandler(CustomException.class)
    public Object CustomExceptionHandler( CustomException e ) {
        log.error( "User(Service)Define Exception[ {} ][ {} ]", e.getErrorCode().getName(), e.getMessage(), e );
//        Sentry.captureException( e );
        return e;
    }

    /**
     * @param e
     * @return Exception
     * 핸들링하지 않은 예외를 상위클래스인 Exception 클래스로 핸들링한다.
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Object unknownExceptionHandler( Exception e ) {
        log.error("Unknown Exception[ {} ]", e.getMessage(), e);
        return new CustomException( e, null );
    }
}
