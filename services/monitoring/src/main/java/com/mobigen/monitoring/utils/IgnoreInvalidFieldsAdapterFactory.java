package com.mobigen.monitoring.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class IgnoreInvalidFieldsAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                try {
                    return delegate.read(in); // 기본 로직을 사용
                } catch (JsonSyntaxException | IllegalStateException e) {
                    // 잘못된 필드를 무시
                    in.skipValue(); // JSON 값 건너뛰기
                    return null;    // 기본 값 반환
                }
            }
        };
    }
}
