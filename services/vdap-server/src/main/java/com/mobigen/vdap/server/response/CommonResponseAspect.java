package com.mobigen.vdap.server.response;

import com.mobigen.vdap.server.exception.CustomException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class CommonResponseAspect {

    @Around("@annotation(com.mobigen.vdap.server.annotations.CommonResponse)")
    public CommonResponse responseJsonSuccess( ProceedingJoinPoint point ) throws Throwable {
        Object results = point.proceed();
        return CommonResponse.builder().code( ResponseCode.SUCCESS.getName() ).data( results ).build();
    }

    @Around("execution(* com.mobigen.vdap.server.response.GlobalExceptionHandler.*(..))")
    public CommonResponse responseJsonFail( ProceedingJoinPoint point ) throws Throwable {
        Object results = point.proceed();
        String errorCode;
        String errorMsg;
        List<String> errorVars = new ArrayList<>();
        if( results instanceof CustomException ) {
            CustomException customException = ( CustomException )results;
            errorCode = ( ( CustomException )results ).getErrorCode().getName();
            errorMsg = messageSource.getMessage( errorCode, customException.getErrorVars(), customException.getMessage() );
            if( customException.getErrorVars() != null && customException.getErrorVars().length > 0 ) {
                errorVars.addAll( Arrays.asList( customException.getErrorVars() ) );
            }
        } else if( results instanceof BindException ) {
            errorCode = ResponseCode.ERROR_BIND.getName();
            errorMsg = messageSource.getMessage( errorCode );
        } else if( results instanceof HttpRequestMethodNotSupportedException  ) {
            errorCode = ResponseCode.ERROR_METHOD_NOT_SUPPORTED.getName();
            errorMsg = messageSource.getMessage( errorCode );
        } else {
            errorCode = ResponseCode.ERROR_UNKNOWN.getName();
            errorMsg = messageSource.getMessage( errorCode );
        }
        return CommonResponse.builder()
                .code( errorCode )
                .errorMsg( errorMsg )
                .errorVars( errorVars )
                .build();
    }
}
