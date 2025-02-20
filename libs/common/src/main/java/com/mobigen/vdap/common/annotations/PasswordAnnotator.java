package com.mobigen.vdap.common.annotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.*;
import org.jsonschema2pojo.AbstractAnnotator;

import java.lang.reflect.Field;
import java.util.TreeMap;

/** Add {@link PasswordField} annotation to generated Java classes */
public class PasswordAnnotator extends AbstractAnnotator {

  /** Add {@link PasswordField} annotation property fields */
  @Override
  public void propertyField(
          JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
    super.propertyField(field, clazz, propertyName, propertyNode);
    if (propertyNode.get("format") != null
        && "password".equals(propertyNode.get("format").asText())) {
      field.annotate(PasswordField.class);
    }
  }

  /** Add {@link PasswordField} annotation to getter methods */
  @Override
  public void propertyGetter(JMethod getter, JDefinedClass clazz, String propertyName) {
    super.propertyGetter(getter, clazz, propertyName);
    addMaskedFieldAnnotationIfApplies(getter, propertyName);
  }

  /** Add {@link PasswordField} annotation to setter methods */
  @Override
  public void propertySetter(JMethod setter, JDefinedClass clazz, String propertyName) {
    super.propertySetter(setter, clazz, propertyName);
    addMaskedFieldAnnotationIfApplies(setter, propertyName);
  }

  /**
   * Use reflection methods to access the {@link JDefinedClass} of the {@link JMethod} object. If
   * the {@link JMethod} is pointing to a field annotated with {@link PasswordField} then annotates
   * the {@link JMethod} object with {@link PasswordField}
   */
  private void addMaskedFieldAnnotationIfApplies(JMethod jMethod, String propertyName) {
    try {
      Field outerClassField = JMethod.class.getDeclaredField("outer");
      outerClassField.setAccessible(true);
      JDefinedClass outerClass = (JDefinedClass) outerClassField.get(jMethod);

      TreeMap<String, JFieldVar> insensitiveFieldsMap =
          new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      insensitiveFieldsMap.putAll(outerClass.fields());

      if (insensitiveFieldsMap.containsKey(propertyName)
          && insensitiveFieldsMap.get(propertyName).annotations().stream()
              .anyMatch(
                  annotation ->
                      PasswordField.class.getName().equals(getAnnotationClassName(annotation)))) {
        jMethod.annotate(PasswordField.class);
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private String getAnnotationClassName(JAnnotationUse annotation) {
    try {
      Field clazzField = JAnnotationUse.class.getDeclaredField("clazz");
      clazzField.setAccessible(true);
      return ((JClass) clazzField.get(annotation)).fullName();
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
