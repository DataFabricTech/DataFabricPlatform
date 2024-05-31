package com.mobigen.dolphin.service;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.CreateModelDto;
import com.mobigen.dolphin.dto.response.ModelDto;
import com.mobigen.dolphin.entity.openmetadata.DBServiceEntity;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import com.mobigen.dolphin.util.ModelType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ModelService {
    private final TrinoRepository trinoRepository;
    private final DolphinConfiguration dolphinConfiguration;

    public List<ModelDto> getModels() {
        return trinoRepository.getModelList();
    }

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

    private String getOrCreateTrinoCatalog(DBServiceEntity dbServiceEntity) {
        var catalogs = trinoRepository.getCatalogs();
        var catalogName = dbServiceEntity.getId().toString().replace("-", "_");
        boolean makeCatalog = true;
        for (var catalog : catalogs) {
            if (catalog.equals(catalogName)) {
                log.info("Already created trino catalog {}", catalogName);
                makeCatalog = false;
                break;
            }
        }
        if (makeCatalog) {
            var createQuery = getQuery(dbServiceEntity, catalogName);
            trinoRepository.execute(createQuery);
        }
        return catalogName;
    }

    private static @NotNull String getQuery(DBServiceEntity dbServiceEntity, String catalogName) {
        var connInfo = dbServiceEntity.getConnection().getConfig();
        var dbms = dbServiceEntity.getServiceType().toLowerCase();
        var jdbcURL = "jdbc:" + dbms + "://" + connInfo.getHostPort();
        if (!List.of("mariadb", "mysql").contains(dbms)  // mariadb/mysql 의 경우 trino 에서 jdbc-url 에 db 세팅을 하지 않도록 되어 있어서 제외
                && !connInfo.getDatabaseName().isEmpty()) {
            jdbcURL = jdbcURL + "/" + connInfo.getDatabaseName();
        }
        var username = connInfo.getUsername();
        var password = connInfo.getPassword();
        return "create catalog " + catalogName
                + " using " + dbms
                + " with ("
                + " \"connection-url\" = '" + jdbcURL + "', "
                + " \"connection-user\" = '" + username + "', "
                + " \"connection-password\" = '" + password + "')";
    }

    public ModelDto createModel(CreateModelDto createModelDto) {
        var selectedColumns = !createModelDto.getBaseModel().getSelectedColumnNames().isEmpty() ?
                createModelDto.getBaseModel().getSelectedColumnNames().stream()
                        .map(dolphinConfiguration.getModel()::convertKeywordName)
                        .collect(Collectors.joining(", "))
                : "*";
        String sql = "create view " + dolphinConfiguration.getModel().getCatalog()
                + "." + dolphinConfiguration.getModel().getSchema()
                + "." + createModelDto.getModelName();

        if (createModelDto.getBaseModel().getType() == ModelType.CONNECTOR) {
            var connInfo = getConnectorInfo(createModelDto.getBaseModel().getConnectorId());
            System.out.println(connInfo);
            var catalogName = getOrCreateTrinoCatalog(connInfo);
            sql = sql + " as select " + selectedColumns
                    + " from " + catalogName
                    + "." + createModelDto.getBaseModel().getDatabase()
                    + "." + createModelDto.getBaseModel().getTable();
        } else if (createModelDto.getBaseModel().getType() == ModelType.MODEL) {
            sql = sql + " as select " + selectedColumns
                    + " from " + dolphinConfiguration.getModel().getCatalog()
                    + "." + dolphinConfiguration.getModel().getSchema()
                    + "." + createModelDto.getBaseModel().getModel();
        } else {
            sql = sql + " as " + createModelDto.getBaseModel().getQuery();
        }
        trinoRepository.execute(sql);
        return ModelDto.builder()
                .name(createModelDto.getModelName())
                .build();
    }
}
