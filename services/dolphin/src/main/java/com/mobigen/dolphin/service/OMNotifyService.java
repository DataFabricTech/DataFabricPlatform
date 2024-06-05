package com.mobigen.dolphin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mobigen.dolphin.dto.request.OMNotifyDto;
import com.mobigen.dolphin.exception.ErrorCode;
import com.mobigen.dolphin.exception.SqlParseException;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
public class OMNotifyService {
    private final ModelService modelService;
    private final OpenMetadataRepository openMetadataRepository;

    public String runDBService(OMNotifyDto omNotifyDto) throws JsonProcessingException {
        if (omNotifyDto.getEventType().equals(OMNotifyDto.EventType.ENTITY_CREATED)) {
            var connInfo = openMetadataRepository.getConnectorInfo(omNotifyDto.getEntityId());
            var catalog = modelService.getOrCreateTrinoCatalog(connInfo);
            return "Success to create catalog [" + catalog + "]";
        } else if (omNotifyDto.getEventType().equals(OMNotifyDto.EventType.ENTITY_DELETED)) {
            log.info("Delete catalog of {}", omNotifyDto.getEntityId());
            modelService.deleteTrinoCatalog(omNotifyDto.getEntityId());
            return "Success to delete catalog [" + omNotifyDto.getEntityId() + "]";
        } else if (omNotifyDto.getEventType().equals(OMNotifyDto.EventType.ENTITY_SOFT_DELETED)
                || omNotifyDto.getEventType().equals(OMNotifyDto.EventType.ENTITY_RESTORED)) {
            log.info("{} of {}", omNotifyDto.getEventType(), omNotifyDto.getEntityId());
            return "Success to " + omNotifyDto.getEventType() + " catalog [" + omNotifyDto.getEntityId() + "]";
        }
        throw new SqlParseException(ErrorCode.UNSUPPORTED, "현재 지원하지 않는 eventType 입니다. " + omNotifyDto.getEventType());
    }

    public String handle(OMNotifyDto omNotifyDto) {
        try {
            if (omNotifyDto.getEntityType().equals(OMNotifyDto.EntityType.DATABASE_SERVICE)) {
                return runDBService(omNotifyDto);
            }
            throw new SqlParseException(ErrorCode.UNSUPPORTED, "현재 지원하지 않는 entityType 입니다. " + omNotifyDto.getEntityType());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
