package com.mobigen.dolphin.service;

import com.mobigen.dolphin.dto.request.OMNotifyDto;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Service
public class OMNotifyService {
    public String runDBService(OMNotifyDto omNotifyDto) {
        if (omNotifyDto.getEventType().equals(OMNotifyDto.EventType.ENTITY_CREATED)) {
            return "create success";
        }
        return "other";
    }
}
