package com.mobigen.libs.grpc.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

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
