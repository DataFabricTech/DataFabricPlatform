package com.mobigen.monitoring.utils;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MapToClassConverterUtil {
    private final Gson gson;

    // ObjectMapper 를 정적 필드로 유지
    private static Gson staticGson;

    // PostConstruct 를 사용하여 정적 필드 초기화
    @PostConstruct
    private void init() {
        staticGson = gson;
    }

    // Static 메서드에서 ObjectMapper 사용
    public static <T> T convertMapToClass(Map<String, Object> map, Class<T> clazz) {
        return staticGson.fromJson(staticGson.toJson(map), clazz);
    }
}
