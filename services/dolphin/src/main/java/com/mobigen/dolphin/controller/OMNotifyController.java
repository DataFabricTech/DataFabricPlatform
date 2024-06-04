package com.mobigen.dolphin.controller;

import com.mobigen.dolphin.dto.request.OMNotifyDto;
import com.mobigen.dolphin.service.OMNotifyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dolphin/open-metadata/notify")
public class OMNotifyController {
    private final OMNotifyService omNotifyService;

    @Operation(summary = "Handle processing of OpenMetadata Notification")
    @PostMapping(value = "", consumes = "application/json")
    public String notify(@RequestBody OMNotifyDto omNotifyDto) {
        if (omNotifyDto.getEntityType().equals("databaseService")) {
            return omNotifyService.runDBService(omNotifyDto);
        }
        return "not implemented";
    }
}
