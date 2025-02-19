package com.mobigen.vdap.server.response;

import com.mobigen.vdap.server.exception.CustomException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class CommonResponseAspect {

    @Around("@annotation(com.mobigen.vdap.server.annotations.CommonResponse)")
    public CommonResponseDto responseJsonSuccess(ProceedingJoinPoint point) throws Throwable {
        Object results = point.proceed();
        return CommonResponseDto.builder().code("success").data(results).build();
    }

    @Around("execution(* com.mobigen.vdap.server.response.GlobalExceptionHandler.*(..))")
    public CommonResponseDto responseJsonFail(ProceedingJoinPoint point) throws Throwable {
        Object results = point.proceed();
        CommonResponseDto response = new CommonResponseDto();
        Map<String, Object> errData = new HashMap<>();
        switch (results) {
            case CustomException customException -> {
                errData.put("error", customException.getClass().getSimpleName());
                errData.put("causedByObject", customException.getCausedObject()); // 예외가 발생한 객체 포함

                // Stacktrace를 String으로 변환하여 포함
                StringWriter sw = new StringWriter();
                customException.printStackTrace(new PrintWriter(sw));
                errData.put("stacktrace", sw.toString());

                response.setCode("Error");
                response.setErrorMsg(customException.getMessage());
                response.setErrorData(errData);
            }
            case BindException exception -> {
                // Stacktrace를 String으로 변환하여 포함
                StringWriter sw = new StringWriter();
                exception.printStackTrace(new PrintWriter(sw));
                errData.put("stacktrace", sw.toString());

                response.setCode("Error");
                response.setErrorMsg(exception.getMessage());
                response.setErrorData(errData);
            }
            case HttpRequestMethodNotSupportedException exception -> {
                // Stacktrace를 String으로 변환하여 포함
                StringWriter sw = new StringWriter();
                exception.printStackTrace(new PrintWriter(sw));
                errData.put("stacktrace", sw.toString());

                response.setCode("Error");
                response.setErrorMsg(exception.getMessage());
                response.setErrorData(errData);
            }
            case null, default -> {
                errData.put("errData", results);
                response.setCode("Error");
                response.setErrorData(errData);
            }
        }
        return response;
    }
}
