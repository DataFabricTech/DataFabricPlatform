package com.mobigen.vdap.schema;

import com.mobigen.vdap.schema.type.Function.ParameterType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Function {
  String name();

  String input();

  String description();

  String[] examples();

  ParameterType paramInputType() default ParameterType.NOT_REQUIRED;
}
