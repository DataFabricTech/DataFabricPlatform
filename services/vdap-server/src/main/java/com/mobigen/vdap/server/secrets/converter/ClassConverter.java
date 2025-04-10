package com.mobigen.vdap.server.secrets.converter;

import com.mobigen.vdap.server.util.JsonUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Currently when an object is converted into a specific class using `JsonUtils.convertValue` there`Object` fields that
 * are not converted into any concrete class which could lead to assign a `LinkedMap` to the `Object` field.
 *
 * <p>This abstract class wrap these `JsonUtils.convertValue` adding transformation to those `Object` fields into
 * specific classes.
 */
public abstract class ClassConverter {

  protected final Class<?> clazz;

  protected ClassConverter(Class<?> clazz) {
    this.clazz = clazz;
  }

  public Object convert(Object object) {
    return JsonUtils.convertValue(object, this.clazz);
  }

  protected Object convert(Object object, Class<?> clazz) {
    try {
      return ClassConverterFactory.getConverter(clazz).convert(object);
    } catch (Exception ignore) {
      // this can be ignored
      return null;
    }
  }

  // method called when we expect only specific class
  protected Optional<Object> tryToConvertOrFail(Object object, List<Class<?>> candidateClasses) {
    if (object != null) {
      Object converted =
          candidateClasses.stream()
              .map(candidateClazz -> convert(object, candidateClazz))
              .filter(Objects::nonNull)
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          String.format(
                              "Cannot convert [%s] due to missing converter implementation.",
                              object.getClass().getSimpleName())));
      return Optional.of(
          ClassConverterFactory.getConverter(converted.getClass()).convert(converted));
    }
    return Optional.empty();
  }

  // method called when and Object field can expect a HashMap or a specific class
  protected Optional<Object> tryToConvert(Object object, List<Class<?>> candidateClasses) {
    if (object != null) {
      Optional<Object> converted =
          candidateClasses.stream()
              .map(candidateClazz -> convert(object, candidateClazz))
              .filter(Objects::nonNull)
              .findFirst();
      if (converted.isPresent()) {
        return Optional.of(
            ClassConverterFactory.getConverter(converted.get().getClass())
                .convert(converted.get()));
      }
    }
    return object == null ? Optional.empty() : Optional.of(object);
  }
}
