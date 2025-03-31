package com.mobigen.monitoring.service.openMetadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mobigen.monitoring.config.OpenMetadataConfig;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.mobigen.monitoring.enums.Common.CONFIG;
import static com.mobigen.monitoring.enums.OpenMetadataEnums.*;

@Component
@Slf4j
public class OpenMetadataService {
    private final OpenMetadataConfig openMetadataConfig;
    private final Utils utils = new Utils();
    private String accessToken;
    private String tokenType;
    private final OkHttpClient client;


    public OpenMetadataService(OpenMetadataConfig openMetadataConfig) {
        this.openMetadataConfig = openMetadataConfig;
        this.client = new OkHttpClient()
                .newBuilder().build();
        getToken();
    }

    public JsonNode get(String endPoint) {
        String sb = this.tokenType +
                " " +
                this.accessToken;
        var url = openMetadataConfig.getOrigin() + endPoint;
        var request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", sb)
                .build();

        try (
                Response response = client.newCall(request).execute();
        ) {
            return utils.getJsonNode(response.body().string());
        } catch (JsonProcessingException e) {
//            throw CommonException.builder()
//                    .errorCode(ErrorCode.JSON_MAPPER_FAIL)
//                    .build();
            throw new CustomException(e.getMessage());
        } catch (IOException e) {
//            throw CommonException.builder()
//                    .errorCode(ErrorCode.GET_FAIL)
//                    .build();
            throw new CustomException(e.getMessage());
        }
    }

    public JsonNode getDatabaseServices() {
        return get(openMetadataConfig.getPath().getDatabaseService() + "?limit=1000").get(DATA.getName());
    }

    public JsonNode getStorageServices() {
        return get(openMetadataConfig.getPath().getStorageService()).get(DATA.getName());
    }

    public JsonNode getTableModels(String endPoint) {
        return get(openMetadataConfig.getPath().getDatabaseModel() + endPoint).get(PAGING.getName());
    }

    public JsonNode getStorageModels(String endPoint) {
        return get(openMetadataConfig.getPath().getStorageModel() + endPoint);
    }

    public JsonNode getAllIngestion() {
        return get(openMetadataConfig.getPath().getIngestionPipeline() + "?limit=1000000").get(DATA.getName());
    }

    public JsonNode getIngestionState(String fqn, String name) {
        var now = LocalDateTime.now().atZone(ZoneId.systemDefault());
        return get(openMetadataConfig.getPath().getIngestionPipeline() + "/" + fqn + "." + name +
                "/pipelineStatus?limit=1000000&startTs=" + now.minusDays(90).toInstant().toEpochMilli() +
                "&endTs=" + now.toInstant().toEpochMilli()).get(DATA.getName());
    }

    public JsonNode getIngestion(UUID ingestionID) {
        return get(openMetadataConfig.getPath().getIngestionPipeline() + "/" + ingestionID).get(DATA.getName());
    }

    public JsonNode getQuery(String param) {
        var queryUrl = openMetadataConfig.getPath().getQuery();

        return get(queryUrl + param);
    }

    public String post(String endPoint, String body) {
        var mediaType = MediaType.parse("application/json");
        var request_body = RequestBody.create(body, mediaType);

        var sb = new StringBuilder();
        sb.append(this.tokenType != null)
                .append(" ")
                .append(this.accessToken);

        var url = openMetadataConfig.getOrigin() + endPoint;
        var requestBuilder = new Request.Builder()
                .url(url)
                .method("POST", request_body);
        if (this.tokenType != null && this.accessToken != null)
            requestBuilder
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", sb.toString());

        var as = requestBuilder.build();

        try (
                Response response = client.newCall(as).execute();
        ) {
            return response.body().string();
        } catch (IOException e) {
//            throw CommonException.builder()
//                    .errorCode(ErrorCode.GET_TOKEN_FAIL)
//                    .build();
            throw new CustomException(e.getMessage());
        }
    }

    public void getToken() {
        // getHostToken
        var id = this.openMetadataConfig.getAuth().getId();
        var pw = this.openMetadataConfig.getAuth().getPasswd();
        var encodePw = Base64.getEncoder().encodeToString(pw.getBytes());

        var token = post(openMetadataConfig.getPath().getLogin(),
                "{\"email\":\"" + id + "\",\"password\":\"" + encodePw + "\"}");
        try {
            var tokenJson = utils.getJsonNode(token);
            this.accessToken = tokenJson.get(ACCESS_TOKEN.getName()).asText();
            this.tokenType = tokenJson.get(TOKEN_TYPE.getName()).asText();
        } catch (JsonProcessingException e) {
//            throw CommonException.builder()
//                    .errorCode(ErrorCode.GET_TOKEN_FAIL)
//                    .build();
            throw new CustomException(e.getMessage());
        }

        // getBotId
        var botIdJson = get(openMetadataConfig.getPath().getBot());
        var botId = botIdJson.get(BOT_USER.getName()).get(ID.getName()).asText();

        var botConfig = get(openMetadataConfig.getPath().getAuthMechanism() + "/" + botId);
        this.accessToken = botConfig.get(CONFIG.getName()).get(JWT_TOKEN.getName()).asText();
    }
}