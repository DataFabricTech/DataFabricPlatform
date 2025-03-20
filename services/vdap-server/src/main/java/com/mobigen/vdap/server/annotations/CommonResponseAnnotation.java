package com.mobigen.vdap.server.annotations;

import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@ResponseBody
public @interface CommonResponseAnnotation {
}