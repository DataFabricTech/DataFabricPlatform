package com.mobigen.monitoring.utils;

import com.google.gson.*;
import com.mobigen.monitoring.exception.CustomException;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;

public class Client {
    public ApiClient getClient() {
        try {
            if (!Utils.isContainer()) {
                File file = new ClassPathResource("config/config").getFile();
                return ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(file))).build();
            }

            ApiClient client = ClientBuilder.cluster().build();

            // 커스터마이징된 Gson 생성
            Gson customGson = new GsonBuilder()
                    .serializeNulls() // Null 값도 직렬화
                    .setLenient()     // JSON의 비표준 형식 허용
                    .registerTypeAdapterFactory(new IgnoreInvalidFieldsAdapterFactory()) // 타입이 맞지 않는 필드 무시
                    .registerTypeAdapter(V1Status.class, new V1StatusAdapter()) // 어댑터 추가
                    .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
                    .create();

            // JSON 클래스에 커스터마이징된 Gson 설정
            JSON customJson = new JSON();
            customJson.setGson(customGson);

            client.setJSON(customJson);

            return client;

        } catch (IOException e) {
            throw new CustomException("Client Error");
        }
    }

    // V1Status 어댑터 정의
    private static class V1StatusAdapter implements JsonDeserializer<V1Status> {
        @Override
        public V1Status deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            V1Status status = new V1Status();

            if (jsonObject.has("status")) {
                JsonElement statusElement = jsonObject.get("status");

                if (statusElement.isJsonPrimitive() && statusElement.getAsJsonPrimitive().isString()) {
                    status.setStatus(statusElement.getAsString()); // 문자열로 처리
                } else {
                    status.setStatus(context.deserialize(statusElement, Object.class).toString()); // 객체로 처리
                }
            }

            // 다른 필드 처리
            if (jsonObject.has("message")) {
                status.setMessage(jsonObject.get("message").getAsString());
            }

            if (jsonObject.has("reason")) {
                status.setReason(jsonObject.get("reason").getAsString());
            }

            return status;
        }
    }
}
