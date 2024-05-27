package com.mobigen.dolphin.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Converter
public class JsonConverter<T> implements AttributeConverter<T, String> {
    protected final ObjectMapper objectMapper;

    public JsonConverter() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (ObjectUtils.isEmpty(attribute)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T convertToEntityAttribute(String s) {
        if (StringUtils.hasText(s)) {
            Class<?> clazz = GenericTypeResolver.resolveTypeArgument(getClass(), JsonConverter.class);
            try {
                return (T) objectMapper.readValue(s, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
