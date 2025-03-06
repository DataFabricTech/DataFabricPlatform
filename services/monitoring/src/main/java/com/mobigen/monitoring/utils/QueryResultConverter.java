package com.mobigen.monitoring.utils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public class QueryResultConverter {

    /**
     * 공통 변환 함수: 쿼리 결과를 제네릭 타입으로 변환
     * @param queryResult 쿼리 실행 결과 (List<Map<String, Object>>)
     * @param keyMapper 키를 구성하는 로직 (entry -> "schema.table" 같은 키를 동적으로 만들 수 있음)
     * @param clazz 변환할 객체의 클래스 타입 (예: TableSchemaInfo.class)
     * @return Map<String, List<T>> 형태로 변환된 결과
     */
    public static <T> Map<String, List<T>> convertToMap(
            List<Map<String, Object>> queryResult,
            Function<Map<String, Object>, String> keyMapper,
            Class<T> clazz
    ) {
        Map<String, List<T>> response = new HashMap<>(); // ✅ 올바른 선언

        for (Map<String, Object> entry : queryResult) {
            String key = keyMapper.apply(entry); // 키 생성 로직

            response.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(mapToClass(entry, clazz)); // 객체 변환 후 추가
        }

        return response;
    }

    /**
     * Map 데이터를 클래스로 변환하는 메서드 (Reflection 사용)
     * @param entry 쿼리 결과의 한 Row(Map<String, Object>)
     * @param clazz 변환할 클래스 타입
     * @return T 타입 객체
     */
    private static <T> T mapToClass(Map<String, Object> entry, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance(); // 객체 생성

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true); // private 필드 접근 허용
                Object value = entry.get(field.getName());

                // null 값 처리 및 기본값 설정
                if (value != null) {
                    field.set(instance, value);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping query result to class " + clazz.getSimpleName(), e);
        }
    }
}
