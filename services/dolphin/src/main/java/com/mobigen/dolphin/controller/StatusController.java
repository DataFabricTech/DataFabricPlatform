package com.mobigen.dolphin.controller;

import com.mobigen.dolphin.config.JobDBConfiguration;
import com.mobigen.dolphin.config.TrinoConfiguration;
import com.mobigen.dolphin.dto.response.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api")
public class StatusController {
    private final JobDBConfiguration jobDBConfiguration;
    private final TrinoConfiguration trinoConfiguration;

    @Operation(summary = "Check the server is available")
    @GetMapping("/status")
    public MessageDto status() {
        if (!jobDBConfiguration.isDBConnected()) {
            return MessageDto.builder()
                    .code(500)
                    .message("Job DB connection is invalid")
                    .build();
        }
        if (!trinoConfiguration.isDBConnected()) {
            return MessageDto.builder()
                    .code(500)
                    .message("trino connection is invalid")
                    .build();
        }
        return MessageDto.builder()
                .code(200)
                .message("OK")
                .build();
    }
}
