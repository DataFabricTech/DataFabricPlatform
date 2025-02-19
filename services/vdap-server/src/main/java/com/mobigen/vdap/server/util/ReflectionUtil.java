package com.mobigen.vdap.server.util;

import com.mobigen.vdap.schema.entity.services.ServiceType;

import java.util.Locale;

public class ReflectionUtil {
    private ReflectionUtil() {
        /* Hidden construction */
    }

//  public static List<Method> getMethodsAnnotatedWith(
//      final Class<?> clazz, final Class<? extends Annotation> annotation) {
//    final List<Method> methods = new ArrayList<>();
//    for (final Method method : clazz.getDeclaredMethods()) {
//      if (method.isAnnotationPresent(annotation)) {
//        methods.add(method);
//      }
//    }
//    return methods;
//  }

    public static Class<?> createConnectionConfigClass(String connectionType, ServiceType serviceType)
            throws ClassNotFoundException {
        String clazzName =
                "com.mobigen.vdap.schema.services.connections."
                        + serviceType.value().toLowerCase(Locale.ROOT)
                        + "."
                        + connectionType
                        + "Connection";
        return Class.forName(clazzName);
    }

//  public static void setValueInMethod(Object toEncryptObject, String fieldValue, Method toSet) {
//    try {
//      toSet.invoke(toEncryptObject, fieldValue);
//    } catch (IllegalAccessException | InvocationTargetException e) {
//      throw new CustomException(e.getMessage(), toEncryptObject);
//    }
//  }
//
//  public static Method getToSetMethod(Object toEncryptObject, Object obj, String fieldName) {
//    try {
//      return toEncryptObject.getClass().getMethod("set" + fieldName, obj.getClass());
//    } catch (NoSuchMethodException e) {
//      throw new ReflectionException(e.getMessage());
//    }
//  }
//
//  public static Object getObjectFromMethod(Method method, Object toEncryptObject) {
//    Object obj;
//    try {
//      obj = method.invoke(toEncryptObject);
//    } catch (IllegalAccessException | InvocationTargetException e) {
//      throw new ReflectionException(e.getMessage());
//    }
//    return obj;
//  }
//
//  public static boolean isGetMethodOfObject(Method method) {
//    return method.getName().startsWith("get")
//        && !method.getReturnType().equals(Void.TYPE)
//        && !method.getReturnType().isPrimitive();
//  }
}
