package com.mobigen.libs.grpc.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * MethodMonitor 에 대한 동작을 정의한 Aspect 구현부
 * 이 정의로 인해 method 의 시작 전/후에 로그를 남김
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@Aspect
public class MethodMonitorAspect {

    @Around("@annotation(MethodMonitor)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        var className = proceedingJoinPoint.getTarget().toString();
        var methodName = proceedingJoinPoint.getSignature().getName();
        var target = className + "." + methodName;
        log.info("Start >> " + target);
        var result = proceedingJoinPoint.proceed();
        log.info("End << " + target);
        return result;
    }
}
