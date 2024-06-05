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
                .header("Authorization", "Bearer eyJraWQiOiJHYjM4OWEtOWY3Ni1nZGpzLWE5MmotMDI0MmJrOTQzNTYiLCJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJvcGVuLW1ldGFkYXRhLm9yZyIsInN1YiI6ImluZ2VzdGlvbi1ib3QiLCJyb2xlcyI6WyJJbmdlc3Rpb25Cb3RSb2xlIl0sImVtYWlsIjoiaW5nZXN0aW9uLWJvdEBvcGVubWV0YWRhdGEub3JnIiwiaXNCb3QiOnRydWUsInRva2VuVHlwZSI6IkJPVCIsImlhdCI6MTcxNTY2MTQ0OSwiZXhwIjpudWxsfQ.c3aaxNApJuE8jlgI6hq0U7vYzN_E0jlfSUAc87JxrYeooC9Nn8PIDoRMN6_D1N3k2AW1Cw8DgEh5ehSPz9EGl9heWV1NwRA1e9UoNCr4sgpMRRqj1k954m9O5WJguLoSOBgEHONKf52HFKGa-arReAyCt3vahEf23lOMwnwTRECzCAWqYTs5dvUhYiRbBvhyhq90DtXXLdaBQRSOALadjn6j_79-NwhuQE4fSKM6SKGZqCuosQaWd0vjECUpObOtI6SvAv8XceIYQHIMKyTGYt1qYyDD1Af8XQ-4fJ1SZFxcYpC7Jpq8pwl9Pu5osaHlzOpCfJ7lh4pJiSZHOCcOIA")
                .retrieve()
                .bodyToMono(DBServiceEntity.class)
                .block();
        log.info(Objects.requireNonNull(response).toString());
        return response;
    }
}
