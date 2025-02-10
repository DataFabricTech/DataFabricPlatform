package com.mobigen.monitoring.aspect;

import com.mobigen.monitoring.dto.response.CommonResponseDto;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.exception.ResponseCode;
import com.mobigen.monitoring.utils.MessageSourceUtil;
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
public class CommonResponseAdvice {

    final MessageSourceUtil messageSource;

    public CommonResponseAdvice(MessageSourceUtil messageSource) {
        this.messageSource = messageSource;
    }

    @Around("@annotation(com.mobigen.monitoring.annotation.CommonResponse)")
    public CommonResponseDto responseJsonSuccess(ProceedingJoinPoint point) throws Throwable {
        Object results = point.proceed();
        return CommonResponseDto.builder().code(ResponseCode.SUCCESS.getName()).data(results).build();
    }

    @Around("execution(* com.mobigen.monitoring.exception.GlobalExceptionAdvice.*(..))")
    public CommonResponseDto responseJsonFail(ProceedingJoinPoint point) throws Throwable {
        Object results = point.proceed();
        String errorCode;
        String errorMsg;
        List<String> errorVars = new ArrayList<>();
        if (results instanceof CustomException) {
            CustomException customException = (CustomException) results;
            errorCode = ((CustomException) results).getErrorCode().getName();
            errorMsg = messageSource.getMessage(errorCode, customException.getErrorVars(), customException.getMessage());
            if (customException.getErrorVars() != null && customException.getErrorVars().length > 0) {
                errorVars.addAll(Arrays.asList(customException.getErrorVars()));
            }
        } else if (results instanceof BindException) {
            errorCode = ResponseCode.ERROR_BIND.getName();
            errorMsg = messageSource.getMessage(errorCode);
        } else if (results instanceof HttpRequestMethodNotSupportedException) {
            errorCode = ResponseCode.ERROR_METHOD_NOT_SUPPORTED.getName();
            errorMsg = messageSource.getMessage(errorCode);
        } else {
            errorCode = ResponseCode.ERROR_UNKNOWN.getName();
            errorMsg = messageSource.getMessage(errorCode);
        }
        return CommonResponseDto.builder()
                .code(errorCode)
                .errorMsg(errorMsg)
                .errorVars(errorVars)
                .build();
    }
}
