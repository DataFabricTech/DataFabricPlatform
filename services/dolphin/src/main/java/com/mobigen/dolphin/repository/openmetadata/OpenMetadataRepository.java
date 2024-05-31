package com.mobigen.dolphin.repository.openmetadata;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.entity.openmetadata.DBServiceEntity;
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
    public DBServiceEntity getConnectorInfo(UUID id) {
        var webClient = WebClient.builder().build();
        var url = dolphinConfiguration.getOpenMetadata().getApiUrl() + "/v1/services/databaseServices/" + id;
        var response = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer eyJraWQiOiJHYjM4OWEtOWY3Ni1nZGpzLWE5MmotMDI0MmJrOTQzNTYiLCJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJvcGVuLW1ldGFkYXRhLm9yZyIsInN1YiI6IkRhdGFJbnNpZ2h0c0FwcGxpY2F0aW9uQm90Iiwicm9sZXMiOltudWxsXSwiZW1haWwiOiJEYXRhSW5zaWdodHNBcHBsaWNhdGlvbkJvdEBvcGVubWV0YWRhdGEub3JnIiwiaXNCb3QiOnRydWUsInRva2VuVHlwZSI6IkJPVCIsImlhdCI6MTcxNTY2MTQ1MiwiZXhwIjpudWxsfQ.u-bZAvCgQ_N_xHA0P49AIDRoVKhQiRBuhOpazSookpFKMOLy3uHQ6uJhRb23uXG8ebMocKQXbJxrbAiOrIPrqUAEmI4m09iiY6KNYUT8gNSx2_EH1_Y8DD2zI9xuVkHdQaA9euffIRpzcab5JHCNFmM2YJsGLNcVWXQuzVt3NAg7IX6Muz-YFSDHxkzFCvxyDqRJFqe4sUzEZpINpdYwFnX3Wg6hzLKQ1kID1E4lifMUmJ6XsKHJVonHnbEGzsidvsrx2UaVn35C8Jj1hu7qlD3EdVkJhRZb4s-h54DezQuY-ZLs-w4-QtZS-rWUo1Gk1oKiG5ZPGh2uyaz9PhsnAA")
                .retrieve()
                .bodyToMono(DBServiceEntity.class)
                .block();
        log.info(Objects.requireNonNull(response).toString());
        return response;
    }
}
