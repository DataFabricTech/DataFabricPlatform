package com.mobigen.libs.grpc.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 각 Method 가 시작되는 시점, 끝나는 시점을 로그로 남기기 위한 어노테이션
 * 이 어노테이션이 붙은 method 는 시작 전/후 에 로그를 남김
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodMonitor {
}
