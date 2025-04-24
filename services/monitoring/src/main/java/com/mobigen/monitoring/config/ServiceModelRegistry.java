package com.mobigen.monitoring.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.mobigen.monitoring.dto.response.fabric.GetDatabasesResponseDto;
import com.mobigen.monitoring.dto.response.fabric.GetObjectStorageResponseDto;
import com.mobigen.monitoring.dto.response.fabric.TableInfoResponseDto;
import com.mobigen.monitoring.vo.ModelInfoVo;
import com.mobigen.monitoring.vo.TableModelInfo;
import com.mobigen.monitoring.vo.TableProfileVo;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * open metadata 에서 받아온 정보들을 in-memory 에 저장하고 범용적으로 사용하기 위한 클래스
 * */
@Configuration
@Getter
public class ServiceModelRegistry {
    // database service 정보
    private final Map<String, GetDatabasesResponseDto> databaseServices = new HashMap<>();

    // storage service 정보
    private final Map<String, GetObjectStorageResponseDto> storageServices = new HashMap<>();

    // table 정보 및 table 개수
    private final Map<String, TableModelInfo> tableModels = new HashMap<>();

    // model 정보
    private final Map<String, ModelInfoVo> serviceModels = new HashMap<>();

    // table rows 정보
    // fqn (table)
    private final Map<String, Map<String, TableProfileVo>> tableRows = new HashMap<>();

    // table info 정보
    // serviceId
    private final Map<String, JsonNode> tableInfosJsonNode = new HashMap<>();

    private final Map<String, Map<String, TableInfoResponseDto>> tableInfos = new HashMap<>();
}
