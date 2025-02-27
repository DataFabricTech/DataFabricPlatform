package com.mobigen.vdap.server.response;

import com.mobigen.vdap.server.exception.CustomException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

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
        CommonResponseDto response = new CommonResponseDto();
        Map<String, Object> errData = new HashMap<>();
        Object results = point.proceed();

        if (results instanceof Exception exception) {
            errData.put("stacktrace", getStackTrace(exception));
            response.setCode("Error");
            response.setErrorMsg(exception.getMessage());

            if (exception instanceof CustomException customException) {
                if (customException.getCausedObject() != null)
                    errData.put("causedByObject", customException.getCausedObject()); // 예외가 발생한 객체 포함
            }
            response.setErrorData(errData);
        } else {
            errData.put("errData", results);
            response.setCode("Error");
            response.setErrorData(errData);
        }
        return response;
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String[] lines = sw.toString().split("\n");

        // 처음 몇 줄과 마지막 몇 줄만 포함
        int keepLines = 3; // 앞뒤로 유지할 라인 개수
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(keepLines, lines.length); i++) {
            sb.append(lines[i]).append("\n");
        }
        sb.append("...\n"); // 중간 생략
        for (int i = Math.max(lines.length - keepLines, keepLines); i < lines.length; i++) {
            sb.append(lines[i]).append("\n");
        }

        return sb.toString();
    }
}
