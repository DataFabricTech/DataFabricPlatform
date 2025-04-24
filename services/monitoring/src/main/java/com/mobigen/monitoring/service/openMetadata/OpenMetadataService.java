package com.mobigen.monitoring.service.openMetadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mobigen.monitoring.config.OpenMetadataConfig;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.exception.ResponseCode;
import com.mobigen.monitoring.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.mobigen.monitoring.enums.Common.CONFIG;
import static com.mobigen.monitoring.enums.OpenMetadataEnum.*;

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

    /**
     * GET Api to open metadata
     * */
    public JsonNode get(String endPoint) {
//        log.debug("[OPEN METADATA] Get endpoint {}", endPoint);

        String sb = this.tokenType +
                " " +
                this.accessToken;
        String url = openMetadataConfig.getOrigin() + endPoint;
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", sb)
                .build();

        try (
                Response response = client.newCall(request).execute();
        ) {
            if (response.body() != null) {
                return utils.getJsonNode(response.body().string());
            } else {
                log.error("[OPEN METADATA] Response of {} api is null", url);

                throw new CustomException(ResponseCode.DFM2000, "OpenMetadata API Response is null");
            }
        } catch (JsonProcessingException e) {
            log.error("[OPEN METADATA] Json form is invalid, url {}", url);

            throw new CustomException(ResponseCode.DFM2001, "Form of response is invalid json form");
        } catch (IOException e) {
            log.error("[OPEN METADATA] API connection error: {}", url);

            throw new CustomException(ResponseCode.DFM3000, String.format("Api [%s] connection error", url), url);
        }
    }

    /**
     * POST Api to open metadata
     * */
    public String post(String endPoint, String body) {
        log.debug("[OPEN METADATA] POST {}", endPoint);
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
            if (response.body() != null) {
                return response.body().string();
            }

            log.error("[OPEN METADATA] POST API connection error: {}", url);

            throw new CustomException(ResponseCode.DFM3000, String.format("Api [%s] connection error", url), url);
        } catch (IOException e) {
            log.error("[OPEN METADATA] POST API connection error: {}", url);

            throw new CustomException(ResponseCode.DFM3000, String.format("Api [%s] connection error", url), url);
        }
    }

    /**
     * Get database service list from open metadata server
     * @return table info list
     * */
    public JsonNode getDatabaseServices() {
        return get(openMetadataConfig.getPath().getDatabaseService() + "?limit=1000").get(DATA.getName());
    }

    /**
     * Get storage service from open metadata server
     * */
    public JsonNode getStorageServices() {
        return get(openMetadataConfig.getPath().getStorageService()).get(DATA.getName());
    }

    /**
     * Get RDB models from open metadata server
     * */
    public JsonNode getTableModels(String endPoint) {
        return get(openMetadataConfig.getPath().getDatabaseModel() + endPoint).get(PAGING.getName());
    }

    /**
     * Get Object storage models from open metadata server
     * */
    public JsonNode getStorageModels(String endPoint) {
        return get(openMetadataConfig.getPath().getStorageModel() + endPoint);
    }

    /**
     * Get Ingestion info
     * */
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

    /**
     * Open metadata 에서 쓰이는 token 정보 가져오는 함수
     * */
    public void getToken() {
        // getHostToken
        log.debug("[OPEN METADATA] Get access token");

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
            log.error("[OPEN METADATA] Failed to parse token");

            throw new CustomException(ResponseCode.DFM2001, "Form of response is invalid json form");
        }

        // getBotId
        var botIdJson = get(openMetadataConfig.getPath().getBot());
        var botId = botIdJson.get(BOT_USER.getName()).get(ID.getName()).asText();

        var botConfig = get(openMetadataConfig.getPath().getAuthMechanism() + "/" + botId);
        this.accessToken = botConfig.get(CONFIG.getName()).get(JWT_TOKEN.getName()).asText();
    }

    /**
     * fqn: database service 의 fully qualified name
     * @return database schema 에 저장되어 있는 모든 table 정보
     * */
    public JsonNode getTableInfo(String fqn) {
        return get(openMetadataConfig.getPath().getTables() + fqn).get(DATA.getName());
    }

    public JsonNode getTableProfile(String fqn) {
        return get(String.format(openMetadataConfig.getPath().getTableProfile(), fqn));
    }
}