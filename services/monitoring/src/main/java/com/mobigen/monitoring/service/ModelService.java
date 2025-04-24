package com.mobigen.monitoring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.monitoring.config.OpenMetadataConfig;
import com.mobigen.monitoring.config.ServiceModelRegistry;
import com.mobigen.monitoring.dto.response.fabric.GetDatabasesResponseDto;
import com.mobigen.monitoring.dto.response.fabric.GetObjectStorageResponseDto;
import com.mobigen.monitoring.dto.response.fabric.ObjectStorageConnectionInfo;
import com.mobigen.monitoring.dto.response.fabric.TableInfoResponseDto;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.exception.ResponseCode;
import com.mobigen.monitoring.service.openMetadata.OpenMetadataService;
import com.mobigen.monitoring.vo.*;
import com.mobigen.monitoring.utils.UnixTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.mobigen.monitoring.enums.DatabaseType.isDatabaseService;
import static com.mobigen.monitoring.enums.OpenMetadataEnum.*;
import static com.mobigen.monitoring.enums.OpenMetadataEnum.ID;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * open metadata 에서 가져온 값을 ServiceModelRegistry 에 저장하는 클래스
 * */
public class ModelService {
    private final OpenMetadataService openMetadataService;

    private final ServiceModelRegistry serviceModelRegistry;
    private final ObjectMapper objectMapper;
    private final OpenMetadataConfig openMetadataConfig;

    /**
     * fabric server 로부터 받아온 데이터를 in-memory 에 저장
     */
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
        final List<JsonNode> data = convertJsonNodeList(databaseServices);

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

    /**
     * table model 정보 불러와서 VO 로 바꾸는 함수
     * return 값이 null 인 경우 database service 가 아닌 경우
     * return 값이 빈 배열인 경우 등록된 모델이 없는 경우
     */
    public List<TableInfoResponseDto> getTableInfos(String serviceId) {
        // fqn 가져오기
        if (serviceModelRegistry.getDatabaseServices().containsKey(serviceId)) {
            String fqn = serviceModelRegistry.getDatabaseServices().get(serviceId).getFullyQualifiedName();

            // Open metadata 에서 table list 가져오기
            final JsonNode tableInfoResponse = openMetadataService.getTableInfo(fqn);

            // 비어 있으면 등록된 모델이 없는 경우
            if (tableInfoResponse.isArray() && tableInfoResponse.isEmpty()) {
                return List.of();
            }

            final List<JsonNode> data = convertJsonNodeList(tableInfoResponse);
            List<TableInfoResponseDto> response = new ArrayList<>();

            // convert json node to VO
            for (JsonNode tableInfoJsonNode : data) {
                response.add(TableInfoResponseDto.of(tableInfoJsonNode));
            }

            return response;
        }

        // service 가 object storage 인 경우
        return null;
    }

    public void setStorageServiceList(JsonNode storageServices) {
        List<JsonNode> data = convertJsonNodeList(storageServices);

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

    // TODO O.M 에서 데이터 받아와서 처리하도록 변경
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

    /**
     * O.M 의 table profile API 에서 받아온 데이터를 저장하는 함수
     * @Param fqn: table fqn
     */
    public void setTableProfile(String serviceId) {
        // O.M 에서 가져온 data 중 rows 만 추출
        if (!serviceModelRegistry.getDatabaseServices().containsKey(serviceId)) {
            return;
        }

        // serviceId 에 해당하는 모든 table fqn 가져와서 요청
        final Map<String, TableInfoResponseDto> tableInfoMap = serviceModelRegistry.getTableInfos().get(serviceId);

        // profile ingestion 이 실행된 적 없으면 데이터가 없음
        if (tableInfoMap == null) {
            log.debug("[Table Profile] Profile ingestion is not activated");
            return;
        }

        for (Map.Entry<String, TableInfoResponseDto> tableInfo : tableInfoMap.entrySet()) {
            String fqn = tableInfo.getKey();

            final JsonNode tableProfile = openMetadataService.getTableProfile(fqn);

            // table profile data 를 저장
            TableProfileVo tableProfileVo = TableProfileVo.builder()
                    .name(tableProfile.get(NAME.getName()).asText())
                    .fullyQualifiedName(tableProfile.get(FULLY_QUALIFIED_NAME.getName()).asText())
                    .updatedAt(Long.parseLong(tableProfile.get(UPDATED_AT.getName()).asText()))
                    .columnCount(Long.parseLong(tableProfile.get(PROFILE.getName()).get(COLUMN_COUNT.getName()).asText().split("\\.")[0]))
                    .rowCount(Long.parseLong(tableProfile.get(PROFILE.getName()).get(ROW_COUNT.getName()).asText().split("\\.")[0]))
                    .build();

            // tableRows 내부 Map 에 값 저장 (누적 or 최초 생성)
            serviceModelRegistry.getTableRows()
                    .computeIfAbsent(serviceId, k -> new HashMap<>())
                    .put(tableProfileVo.getName(), tableProfileVo);
        }
    }

    // table 개수 가져오는 함수
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

    private List<JsonNode> convertJsonNodeList(JsonNode jsonNode) {
        List<JsonNode> data = new ArrayList<>();

        if (jsonNode != null && jsonNode.isArray()) {  // 배열인지 확인
            for (JsonNode node : jsonNode) {
                data.add(node);
            }
        }

        return data;
    }
}
