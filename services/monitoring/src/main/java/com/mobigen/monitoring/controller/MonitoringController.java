package com.mobigen.monitoring.controller;

import com.mobigen.monitoring.annotation.CommonResponse;
import com.mobigen.monitoring.dto.request.TaskId;
import com.mobigen.monitoring.dto.response.CommonResponseDto;
import com.mobigen.monitoring.enums.DatabaseType;
import com.mobigen.monitoring.service.*;
import com.mobigen.monitoring.service.monitoring.MonitoringService;
import com.mobigen.monitoring.service.timer.TaskInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {
    private final MonitoringService monitoringService;
    private final ConnectionService connectionService;
    private final ServicesService servicesService;


    public MonitoringController(final ServicesService servicesService, final ConnectionService connectionService, final ConnectionHistoryService connectionHistoryService, final ModelRegistrationService modelRegistrationService, final MetadataService metadataService, final IngestionHistoryService ingestionHistoryService, final ServicesService servicesService1) {
        this.connectionService = connectionService;
        this.servicesService = servicesService1;
        this.monitoringService = new MonitoringService(null);
    }

    @GetMapping("/start")
    @CommonResponse
    public Object start() {
        return monitoringService.start();
    }

    @PostMapping("/edit")
    @CommonResponse
    public Object editTask(@RequestBody TaskInfo body) {
        return monitoringService.addTask(body);
    }

    @PostMapping("/remove")
    @CommonResponse
    public Object removeTask(@RequestBody TaskId body) {
        return monitoringService.removeTask(body.getId());
    }

    @GetMapping("/service-type")
    @CommonResponse
    public Object serviceType() {
        return DatabaseType.values();
    }

    // ko monitoring
    @Operation(
            operationId = "statusCheck",
            summary = "Status Check",
            description =
                    "모니터링의 상태를 확인하기 위한 API 입니다."
    )
    @GetMapping("/statusCheck")
    public void statusCheck() {
        /* *
         * TODO monitoring status check
         * */
    }

    // Services
    @Operation(
            operationId = "connectStatus",
            summary = "Connect Status",
            description =
                    "모든 서비스들의 연결 상태를 위한 API",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "서비스들의 연결 상태에 대한 정보",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schemaProperties = {
                                            @SchemaProperty(name = "data",
                                                    schema = @Schema(implementation = CommonResponseDto.class)
                                            )
                                    }
                            )
                    )
            })
    @GetMapping("/connectStatus/summary")
    public Object connectStatusSummary() {
        return connectionService.getConnectionStatusSummary();
    }

    @Operation(
            operationId = "connectStatus",
            summary = "Connect Status",
            description =
                    "모든 서비스들의 연결 상태를 위한 API",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "서비스들의 연결 상태에 대한 정보",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schemaProperties = {
                                            @SchemaProperty(name = "data",
                                                    schema = @Schema(implementation = CommonResponseDto.class)
                                            )
                                    }
                            )
                    )
            })
    @GetMapping("/connectStatus")
    public Object connectStatus(
            @Parameter(description = "서비스의 삭제 유무를 위한 매개변수",
                    schema = @Schema(type = "boolean", example = "true"))
            @RequestParam(value = "deleted", required = false,
                    defaultValue = "false") boolean deleted,
            @RequestParam(value = "pageNumber", required = false,
                    defaultValue = "${pageable-config.registration.page_number}") @Min(0) int pageNumber,
            @Parameter(description = "한 페이지에 표시할 데이터의 수를 나타내는 매개변수",
                    schema = @Schema(type = "int", example = "5"))
            @RequestParam(value = "pageSize", required = false,
                    defaultValue = "${pageable-config.registration.page_size}") @Min(1) int pageSize) {
        return servicesService.getServices(
                deleted,
                PageRequest.of(
                        pageNumber,
                        pageSize,
                        Sort.by("createdAt").descending()
                )
        );
    }
}
