package com.mobigen.dolphin.service;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.CreateModelDto;
import com.mobigen.dolphin.dto.request.CreateModelWithFileDto;
import com.mobigen.dolphin.dto.response.ModelDto;
import com.mobigen.dolphin.entity.openmetadata.OMDBServiceEntity;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import com.mobigen.dolphin.util.Functions;
import com.mobigen.dolphin.util.ModelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
    private final OpenMetadataRepository openMetadataRepository;

    public List<ModelDto> getModels() {
        return trinoRepository.getModelList();
    }

    public String getOrCreateTrinoCatalog(OMDBServiceEntity OMDBServiceEntity) {
        var catalogs = trinoRepository.getCatalogs();
        var catalogName = Functions.getCatalogName(OMDBServiceEntity.getId());
        boolean makeCatalog = true;
        for (var catalog : catalogs) {
            if (catalog.equals(catalogName)) {
                log.info("Already created trino catalog {}", catalogName);
                makeCatalog = false;
                break;
            }
        }
        if (makeCatalog) {
            var connInfo = OMDBServiceEntity.getConnection().getConfig();
            var username = connInfo.getUsername();
            String dbms;
            String password;
            if ("postgres".equalsIgnoreCase(OMDBServiceEntity.getServiceType())) {
                dbms = "postgresql";
                password = connInfo.getAuthType().getPassword();
            } else {
                dbms = OMDBServiceEntity.getServiceType().toLowerCase();
                password = connInfo.getPassword();
            }
            var jdbcURL = "jdbc:" + dbms + "://" + connInfo.getHostPort();
            if (!List.of("mariadb", "mysql").contains(dbms)  // mariadb/mysql 의 경우 trino 에서 jdbc-url 에 db 세팅을 하지 않도록 되어 있어서 제외
                    && !connInfo.getDatabase().isEmpty()) {
                jdbcURL = jdbcURL + "/" + connInfo.getDatabase();
            }

            var createQuery = "create catalog " + catalogName
                    + " using " + dbms
                    + " with ("
                    + " \"connection-url\" = '" + jdbcURL + "', "
                    + " \"connection-user\" = '" + username + "', "
                    + " \"connection-password\" = '" + password + "')";
            trinoRepository.execute(createQuery);
        }
        return catalogName;
    }

    public void deleteTrinoCatalog(UUID entityId) {
        var catalogName = Functions.getCatalogName(entityId);
        var deleteQuery = "drop catalog if exists " + catalogName;
        trinoRepository.execute(deleteQuery);
    }

    public ModelDto createModel(CreateModelDto createModelDto) {
        var selectedColumns = !createModelDto.getBaseModel().getSelectedColumnNames().isEmpty() ?
                createModelDto.getBaseModel().getSelectedColumnNames().stream()
                        .map(Functions::convertKeywordName)
                        .collect(Collectors.joining(", "))
                : "*";
        String sql = "create view " + dolphinConfiguration.getModel().getCatalog()
                + "." + dolphinConfiguration.getModel().getDBSchema()
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
                    + "." + dolphinConfiguration.getModel().getDBSchema()
                    + "." + createModelDto.getBaseModel().getModel();
        } else {
            sql = sql + " as " + createModelDto.getBaseModel().getQuery();
        }
        trinoRepository.execute(sql);
        return ModelDto.builder()
                .name(createModelDto.getModelName())
                .build();
    }

    public ModelDto createModelWithFile(CreateModelWithFileDto createModelDto, MultipartFile file) {
        // TODO : upload file to s3(minio) - FMS 로 대체 가능?
        // TODO : create model using trino-hive-s3(minio)

//        String sql = "create table " + dolphinConfiguration.getModel().getCatalog()
//                + "." + dolphinConfiguration.getModel().getDBSchema()
//                + "." + createModelDto.getModelName();
//        sql = sql + " as select " + selectedColumns
//        + " from " + catalogName
//                + "." + createModelDto.getBaseModel().getDatabase()
//                + "." + createModelDto.getBaseModel().getTable();
        return ModelDto.builder()
                .name(createModelDto.getModelName())
                .build();
    }
}
