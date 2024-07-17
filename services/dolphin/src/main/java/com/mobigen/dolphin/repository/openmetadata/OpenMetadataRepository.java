package com.mobigen.dolphin.repository.openmetadata;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.entity.openmetadata.OMDBServiceEntity;
import com.mobigen.dolphin.entity.openmetadata.OMTableEntity;
import jakarta.validation.constraints.AssertTrue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class OpenMetadataRepository {
    private final DolphinConfiguration dolphinConfiguration;

    @AssertTrue(message = "Fail to get databaseService information from OpenMetadata")
    public OMDBServiceEntity getConnectorInfo(UUID id) {
        var webClient = WebClient.builder().build();
        var url = dolphinConfiguration.getOpenMetadata().getApiUrl() + "/v1/services/databaseServices/" + id;
        var response = webClient.get()
                .uri(url)
                .header("Authorization", dolphinConfiguration.getOpenMetadata().getBotToken())
                .retrieve()
                .bodyToMono(OMDBServiceEntity.class)
                .block();
        log.info(Objects.requireNonNull(response).toString());
        return response;
    }

    @AssertTrue(message = "Fail to get table information from OpenMetadata")
    public OMTableEntity getTable(UUID id) {
        var webClient = WebClient.builder().build();
        var url = dolphinConfiguration.getOpenMetadata().getApiUrl() + "/v1/tables/" + id;
        var response = webClient.get()
                .uri(url)
                .header("Authorization", dolphinConfiguration.getOpenMetadata().getBotToken())
                .retrieve()
                .bodyToMono(OMTableEntity.class)
                .block();
        log.info(Objects.requireNonNull(response).toString());
        return response;
    }

    @AssertTrue(message = "Fail to get table information from OpenMetadata")
    public OMTableEntity getTable(String fullyQualifiedName) {
        var webClient = WebClient.builder().build();
        var url = dolphinConfiguration.getOpenMetadata().getApiUrl() + "/v1/tables/name/" + fullyQualifiedName;
        var response = webClient.get()
                .uri(url)
                .header("Authorization", dolphinConfiguration.getOpenMetadata().getBotToken())
                .retrieve()
                .bodyToMono(OMTableEntity.class)
                .block();
        log.info(Objects.requireNonNull(response).toString());
        return response;
    }
}
