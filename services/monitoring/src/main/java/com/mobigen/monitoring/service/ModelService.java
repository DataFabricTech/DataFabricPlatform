package com.mobigen.monitoring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.monitoring.config.OpenMetadataConfig;
import com.mobigen.monitoring.config.ServiceModelRegistry;
import com.mobigen.monitoring.dto.response.fabric.GetDatabasesResponseDto;
import com.mobigen.monitoring.dto.response.fabric.GetObjectStorageResponseDto;
import com.mobigen.monitoring.dto.response.fabric.ObjectStorageConnectionInfo;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.exception.ResponseCode;
import com.mobigen.monitoring.service.openMetadata.OpenMetadataService;
import com.mobigen.monitoring.vo.DatabaseConnectionInfo;
import com.mobigen.monitoring.utils.UnixTimeUtil;
import com.mobigen.monitoring.vo.ModelInfoVo;
import com.mobigen.monitoring.vo.TableModelInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mobigen.monitoring.enums.DatabaseType.isDatabaseService;
import static com.mobigen.monitoring.enums.OpenMetadataEnum.*;
import static com.mobigen.monitoring.enums.OpenMetadataEnum.ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelService {
    private final OpenMetadataService openMetadataService;

    private final ServiceModelRegistry serviceModelRegistry;
    private final ObjectMapper objectMapper;
    private final OpenMetadataConfig openMetadataConfig;

    /**
     * fabric server 로부터 받아온 데이터를 in-memory 에 저장
     * */
    public List<JsonNode> getServiceListFromFabricServer() {
        JsonNode databaseServices = openMetadataService.getDatabaseServices();
        JsonNode storageServices = openMetadataService.getStorageServices();

        List<JsonNode> currentServices = new ArrayList<>();

        // merge list
        databaseServices.forEach(currentServices::add);
        storageServices.forEach(currentServices::add);

        // service 저장
        setDatabaseServices(databaseServices);
        setStorageServiceList(storageServices);

        // model 개수 저장
        setServiceModels(currentServices);

        return currentServices;
    }

    public void setDatabaseServices(JsonNode databaseServices) {
        List<JsonNode> data = new ArrayList<>();

        if (databaseServices != null && databaseServices.isArray()) {  // 배열인지 확인
            for (JsonNode node : databaseServices) {
                data.add(node);
            }
        }

        for (JsonNode databaseService : data) {
            try {
                GetDatabasesResponseDto value = GetDatabasesResponseDto.builder()
                        .id(databaseService.get(ID.getName()).asText())
                        .name(databaseService.get(NAME.getName()).asText())
                        .description(databaseService.get(DESCRIPTION.getName()).asText())
                        .fullyQualifiedName(databaseService.get(FULLY_QUALIFIED_NAME.getName()).asText())
                        .updatedAt(Long.valueOf(databaseService.get(UPDATED_AT.getName()).toString()))
                        .updatedBy(databaseService.get(UPDATED_BY.getName()).asText())
                        .serviceType(databaseService.get(SERVICE_TYPE.getName()).asText())
                        .connection(objectMapper.readValue(databaseService.get(CONNECTION.getName()).get(CONFIG.getName()).toString(), DatabaseConnectionInfo.class))
                        .password(databaseService.get(PASSWORD.getName()) == null ? null : databaseService.get(PASSWORD.getName()).asText())
                        .deleted(databaseService.get(DELETED.getName()).asBoolean())
                        .build();

                this.serviceModelRegistry.getDatabaseServices().put(
                        databaseService.get(ID.getName()).toString().replace("\"", ""),
                        value
                );
            } catch (JsonProcessingException e) {
                throw new CustomException(ResponseCode.DFM2001, "Form of response is invalid json form");
            }
        }
    }

    public void setStorageServiceList(JsonNode storageServices) {
        List<JsonNode> data = new ArrayList<>();

        if (storageServices != null && storageServices.isArray()) {  // 배열인지 확인
            for (JsonNode node : storageServices) {
                data.add(node);
            }
        }

        for (JsonNode storageService : data) {
            try {
                this.serviceModelRegistry.getStorageServices().put(
                        storageService.get(ID.getName()).asText(),
                        GetObjectStorageResponseDto.builder()
                                .id(storageService.get(ID.getName()).asText())
                                .name(storageService.get(NAME.getName()).asText())
                                .description(storageService.get(DESCRIPTION.getName()).asText())
                                .fullyQualifiedName(storageService.get(NAME.getName()).asText())
                                .updatedAt(Long.valueOf(storageService.get(UPDATED_AT.getName()).asText()))
                                .updatedBy(storageService.get(UPDATED_BY.getName()).asText())
                                .serviceType(storageService.get(SERVICE_TYPE.getName()).asText())
                                .deleted(storageService.get(DELETED.getName()).asBoolean())
                                .connection(objectMapper.readValue(storageService.get(CONNECTION.getName()).get(CONFIG.getName()).toString(), ObjectStorageConnectionInfo.class))
                                .build()
                );
            } catch (JsonProcessingException e) {
                throw new CustomException(ResponseCode.DFM2001, "Form of response is invalid json form");
            }
        }
    }

    public void setServiceModels(final List<JsonNode> currentServices) {
        // table 개수 세기
        // om의 model 개수 세기
        for (JsonNode currentService : currentServices) {
            final String serviceType = currentService.get(SERVICE_TYPE.getName()).asText();
            JsonNode models;

            // RDB 데이터베이스일 경우
            if (isDatabaseService(serviceType)) {
                models = openMetadataService.getTableModels(
                        currentService.get(
                                FULLY_QUALIFIED_NAME.getName()
                        ).asText()
                );
            } else {
                models = openMetadataService.getStorageModels(
                        currentService.get(
                                NAME.getName()
                        ).asText()
                ).get(PAGING.getName());
            }

            final Long total = Long.valueOf(models.get("total").asText());

            serviceModelRegistry.getServiceModels().put(
                    currentService.get(ID.getName()).asText(),
                    ModelInfoVo.builder()
                            .total(total)
                            .updatedAt(UnixTimeUtil.getCurrentMillis())
                            .build()
            );
        }
    }

    public TableModelInfo getModelCountFromOM(final UUID serviceID) {
        if (serviceModelRegistry.getDatabaseServices().get(serviceID.toString()) == null) {
            log.error("serviceID: {}", serviceID);
        }
        String fullyQualifiedName = serviceModelRegistry.getDatabaseServices().get(serviceID.toString()).getFullyQualifiedName();

        final JsonNode tableModels = openMetadataService.getTableModels(fullyQualifiedName);

        final TableModelInfo tableModelInfo = objectMapper.convertValue(tableModels, TableModelInfo.class);

        this.serviceModelRegistry.getTableModels().put(serviceID.toString(), tableModelInfo);

        return tableModelInfo;
    }

    public Integer getStorageModelCountFromOM(final UUID serviceId) {
        try {
            final GetObjectStorageResponseDto getObjectStorageResponseDto = serviceModelRegistry.getStorageServices().get(serviceId.toString());

            final JsonNode storageModels = openMetadataService.getStorageModels(openMetadataConfig.getPath().getStorageModel() + getObjectStorageResponseDto.getName());

            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(storageModels.traverse());

            // children 배열의 개수 세기
            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isArray() && !dataNode.isEmpty()) {
                JsonNode childrenNode = dataNode.get(0).path("children");
                int childrenCount = childrenNode.isArray() ? childrenNode.size() : 0;

                log.info("Children: {}", childrenCount);

                return childrenCount;
            } else {
                log.info("data: {}", dataNode);
                return 0;
            }
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }
}
