package com.mobigen.dolphin.service;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.CreateModelDto;
import com.mobigen.dolphin.dto.response.ModelDto;
import com.mobigen.dolphin.entity.openmetadata.DBServiceEntity;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import com.mobigen.dolphin.util.ModelType;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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
    private final OpenMetadataRepository openMetadataRepository;

    public List<ModelDto> getModels() {
        return trinoRepository.getModelList();
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
            var connInfo = openMetadataRepository.getConnectorInfo(createModelDto.getBaseModel().getConnectorId());
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
