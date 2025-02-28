package com.mobigen.monitoring.controller;

import com.mobigen.monitoring.annotation.CommonResponse;
import com.mobigen.monitoring.domain.ConnectionHistory;
import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.dto.request.TaskId;
import com.mobigen.monitoring.dto.response.CommonResponseDto;
import com.mobigen.monitoring.enums.DatabaseType;
import com.mobigen.monitoring.service.ConnectionService;
import com.mobigen.monitoring.service.monitoring.MonitoringService;
import com.mobigen.monitoring.service.storage.*;
import com.mobigen.monitoring.service.scheduler.TaskInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {
    private final MonitoringService monitoringService;
    private final ConnectionService connectionService;
    private final ServicesService servicesService;
    private final ConnectionHistoryService connectionHistoryService;
    private final ModelRegistrationService modelRegistrationService;
    private final IngestionHistoryService ingestionHistoryService;


    public MonitoringController(
            final ConnectionService connectionService,
            final ConnectionHistoryService connectionHistoryService,
            final ServicesService servicesService,
            final ModelRegistrationService modelRegistrationService,
            final IngestionHistoryService ingestionHistoryService
            ) {
        this.connectionService = connectionService;
        this.servicesService = servicesService;
        this.connectionHistoryService = connectionHistoryService;
        this.modelRegistrationService = modelRegistrationService;
        this.ingestionHistoryService = ingestionHistoryService;
        this.monitoringService = new MonitoringService();
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
    @CommonResponse
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
    @CommonResponse
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

    @Operation(
            operationId = "targetConnectStatus",
            summary = "Target Connect Status",
            description =
                    "특정 서비스의 연결 상태를 위한 API",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "특정 서비스의 연결 상태에 대한 히스토리 정보",
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
    @CommonResponse
    @GetMapping("/connectStatus/{serviceID}")
    public Object connectStatus(
            @Parameter(description = "연결 상태 히스토리를 얻을 서비스의 아이디",
                    schema = @Schema(type = "string"))
            @PathVariable("serviceID") String serviceID,
            @Parameter(description = "요청된 데이터의 페이지 번호를 위한 매개변수",
                    schema = @Schema(type = "int", example = "0"))
            @RequestParam(value = "pageNumber", required = false,
                    defaultValue = "${pageable-config.connect.page_number}") @Min(0) int pageNumber,
            @Parameter(description = "한 페이지에 표시할 데이터의 수를 나타내는 매개변수",
                    schema = @Schema(type = "int", example = "5"))
            @RequestParam(value = "pageSize", required = false,
                    defaultValue = "${pageable-config.connect.page_size}") @Min(1) int pageSize) {

        UUID serviceId = UUID.fromString(serviceID);

        Optional<Services> serviceOpt = servicesService.getServices(serviceId);
        final List<ConnectionHistory> connectionHistories = connectionHistoryService.getConnectionHistories(serviceId, PageRequest.of(pageNumber, pageSize));

        return connectionService.getConnectStatus(serviceOpt, connectionHistories);
    }

    @Operation(
            operationId = "avgResponseTime",
            summary = "Average Response Time",
            description =
                    "모든 서비스들의 평균 응답 시간을 얻기 위한 API",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "모든 서비스들의 평균 응답 시간 정보",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schemaProperties = {
                                            @SchemaProperty(name = "totalSize",
                                                    schema = @Schema(implementation = Long.class)),
                                            @SchemaProperty(name = "data",
                                                    array = @ArraySchema(
                                                            schema = @Schema(implementation = CommonResponseDto.class)))
                                    }
                            )
                    )
            })
    @CommonResponse
    @GetMapping("/avgResponseTime")
    public Object avgResponseTimes(
            @Parameter(description = "평균 응답 시간의 내림차순 혹은 오름차순을 정하기 위한 매개변수",
                    schema = @Schema(type = "boolean", example = "true"))
            @RequestParam(value = "orderByAsc", required = false,
                    defaultValue = "false") boolean orderBy,
            @Parameter(description = "서비스의 삭제 유무를 위한 매개변수",
                    schema = @Schema(type = "boolean", example = "true"))
            @RequestParam(value = "deleted", required = false,
                    defaultValue = "false") boolean deleted,
            @Parameter(description = "요청된 데이터의 페이지 번호를 위한 매개변수",
                    schema = @Schema(type = "int", example = "0"))
            @RequestParam(value = "pageNumber", required = false,
                    defaultValue = "${pageable-config.connect.page_number}") @Min(0) int pageNumber,
            @Parameter(description = "한 페이지에 표시할 데이터의 수를 나타내는 매개변수",
                    schema = @Schema(type = "int", example = "5"))
            @RequestParam(value = "pageSize", required = false,
                    defaultValue = "${pageable-config.connect.page_size}") @Min(1) int pageSize) {
        PageRequest pageRequest = PageRequest.of(
                pageNumber,
                pageSize,
                orderBy ? Sort.by("queryExecutionTime").ascending() : Sort.by("queryExecutionTime").descending()
        );
        return connectionService.getAvgResponseTimes(deleted, pageRequest);
    }

    @Operation(
            operationId = "recResponseTime",
            summary = "Recent Response Time",
            description =
                    "모든 서비스들의 최신 응답 시간을 얻기 위한 API",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "모든 서비스들의 최신 응답 시간 정보",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schemaProperties = {
                                            @SchemaProperty(name = "totalSize",
                                                    schema = @Schema(implementation = Long.class)),
                                            @SchemaProperty(name = "data",
                                                    array = @ArraySchema(
                                                            schema = @Schema(implementation = CommonResponseDto.class)))
                                    }
                            )
                    )
            })
    @CommonResponse
    @GetMapping("/recResponseTime")
    public Object recentResponseTimes(
            @Parameter(description = "최신 응답 시간의 내림차순 혹은 오름차순을 정하기 위한 매개변수",
                    schema = @Schema(type = "boolean", example = "true"))
            @RequestParam(value = "orderByAsc", required = false,
                    defaultValue = "false") boolean orderBy,
            @Parameter(description = "서비스의 삭제 유무를 위한 매개변수",
                    schema = @Schema(type = "boolean", example = "true"))
            @RequestParam(value = "deleted", required = false,
                    defaultValue = "false") boolean deleted,
            @Parameter(description = "요청된 데이터의 페이지 번호를 위한 매개변수",
                    schema = @Schema(type = "int", example = "0"))
            @RequestParam(value = "pageNumber", required = false,
                    defaultValue = "${pageable-config.connect.page_number}") @Min(0) int pageNumber,
            @Parameter(description = "한 페이지에 표시할 데이터의 수를 나타내는 매개변수",
                    schema = @Schema(type = "int", example = "5"))
            @RequestParam(value = "pageSize", required = false,
                    defaultValue = "${pageable-config.connect.page_size}") @Min(1) int pageSize) {
        PageRequest pageRequest = PageRequest.of(
                pageNumber,
                pageSize,
                orderBy ? Sort.by("queryExecutionTime").ascending() : Sort.by("queryExecutionTime").descending()
        );

        return connectionService.getRecentResponseTime(deleted, pageRequest);
    }

    @Operation(
            operationId = "targetResponseTime",
            summary = "Target Response Time",
            description =
                    "특정 서비스의 응답 시간 히스토리를 얻기 위한 API",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "특정 서비스들의 응답 시간 히스토리 정보",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schemaProperties = {
                                            @SchemaProperty(name = "totalSize",
                                                    schema = @Schema(implementation = Long.class)),
                                            @SchemaProperty(name = "data",
                                                    array = @ArraySchema(
                                                            schema = @Schema(implementation = CommonResponseDto.class)))
                                    }
                            )
                    )
            })
    @CommonResponse
    @GetMapping("/responseTime/{serviceID}")
    public Object targetResponseTimes(
            @Parameter(description = "응답 시간 히스토리를 얻을 특정 서비스의 아이디",
                    schema = @Schema(type = "string"))
            @PathVariable("serviceID") String serviceID,
            @Parameter(description = "요청된 데이터의 페이지 번호를 위한 매개변수",
                    schema = @Schema(type = "int", example = "0"))
            @RequestParam(value = "pageNumber", required = false,
                    defaultValue = "${pageable-config.connect.page_number}") int pageNumber,
            @Parameter(description = "한 페이지에 표시할 데이터의 수를 나타내는 매개변수",
                    schema = @Schema(type = "int", example = "5"))
            @RequestParam(value = "pageSize", required = false,
                    defaultValue = "${pageable-config.connect.page_size}") int pageSize
    ) {
        UUID serviceId = UUID.fromString(serviceID);
        PageRequest pageRequest = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by("queryExecutionTime").descending()
        );

        return connectionService.getResponseTimes(serviceId, pageRequest);
    }

    @Operation(
            operationId = "connectionHistory",
            summary = "ConnectionDao History",
            description =
                    "연결 상태 히스토리를 위한 API",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "연결 상태 히스토리 정보",
                            content = @Content(
                                    mediaType = "application/json",
                                    schemaProperties = {
                                            @SchemaProperty(name = "data",
                                                    array = @ArraySchema(
                                                            schema = @Schema(implementation = CommonResponseDto.class)))
                                    }
                            )
                    )
            })
    @CommonResponse
    @GetMapping("/connectionHistory")
    public Object connectionHistory(
            @Parameter(description = "요청된 데이터의 페이지 번호를 위한 매개변수",
                    schema = @Schema(type = "int", example = "0"))
            @RequestParam(value = "pageNumber", required = false,
                    defaultValue = "${pageable-config.connect.page_number}") @Min(0) int pageNumber,
            @Parameter(description = "한 페이지에 표시할 데이터의 수를 나타내는 매개변수",
                    schema = @Schema(type = "int", example = "30"))
            @RequestParam(value = "pageSize", required = false,
                    defaultValue = "${pageable-config.connect.page_size}") @Min(1) int pageSize) {
        PageRequest pageRequest = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by("updated_at").descending()
        );

        return connectionHistoryService.getAllConnectionHistory(pageRequest);
    }

    @Operation(
            operationId = "targetConnectionHistory",
            summary = "Target ConnectionDao History",
            description =
                    "특정 서비스의 히스토리를 위한 API",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "특정 서비스의 연결 상태 히스토리 정보",
                            content = @Content(
                                    mediaType = "application/json",
                                    schemaProperties = {
                                            @SchemaProperty(name = "data",
                                                    schema = @Schema(implementation = CommonResponseDto.class)
                                            )
                                    }
                            )
                    )
            })
    @CommonResponse
    @GetMapping("/connectionHistory/{serviceID}")
    public Object connectionHistory(
            @Parameter(description = "히스토리를 얻을 특정 서비스의 아이디",
                    schema = @Schema(type = "string"))
            @PathVariable("serviceID") String serviceID,
            @Parameter(description = "요청된 데이터의 페이지 번호를 위한 매개변수",
                    schema = @Schema(type = "int", example = "0"))
            @RequestParam(value = "pageNumber", required = false,
                    defaultValue = "${pageable-config.connection-history.page_number}") @Min(0) int pageNumber,
            @Parameter(description = "한 페이지에 표시할 데이터의 수를 나타내는 매개변수",
                    schema = @Schema(type = "int", example = "5"))
            @RequestParam(value = "pageSize", required = false,
                    defaultValue = "${pageable-config.connection-history.page_size}") @Min(1) int pageSize
    ) {
        final UUID serviceId = UUID.fromString(serviceID);
        final PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        return connectionHistoryService.getConnectionHistory(serviceId, pageRequest);
    }

    @Operation(
            operationId = "model",
            summary = "Registrations of Model",
            description =
                    "데이터 모델 등록 현황 API",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "데이터 모델의 등록 현황 정보",
                            content = @Content(
                                    mediaType = "application/json",
                                    schemaProperties = {
                                            @SchemaProperty(name = "totalSize",
                                                    schema = @Schema(implementation = Long.class)),
                                            @SchemaProperty(name = "data",
                                                    array = @ArraySchema(
                                                            schema = @Schema(implementation = CommonResponseDto.class)))
                                    })
                    )
            })
    @CommonResponse
    @GetMapping("/models")
    public Object models(
            @Parameter(description = "평균 응답 시간의 내림차순 혹은 오름차순을 정하기 위한 매개변수",
                    schema = @Schema(type = "boolean", example = "true"))
            @RequestParam(value = "orderByAsc", required = false,
                    defaultValue = "false") boolean orderBy,
            @Parameter(description = "서비스의 삭제 유무를 위한 매개변수",
                    schema = @Schema(type = "boolean", example = "true"))
            @RequestParam(value = "deleted", required = false,
                    defaultValue = "false") boolean deleted,
            @RequestParam(value = "pageNumber", required = false,
                    defaultValue = "${pageable-config.registration.page_number}") @Min(0) int pageNumber,
            @Parameter(description = "한 페이지에 표시할 데이터의 수를 나타내는 매개변수",
                    schema = @Schema(type = "int", example = "5"))
            @RequestParam(value = "pageSize", required = false,
                    defaultValue = "${pageable-config.registration.page_size}") @Min(1) int pageSize
    ) {
        final PageRequest pageRequest = PageRequest.of(
                pageNumber,
                pageSize,
                orderBy ? Sort.by("updated_at").ascending() : Sort.by("updated_at").descending());

        return modelRegistrationService.getAllModelRegistration(deleted, pageRequest);
    }

    @Operation(
            operationId = "ingestionHistory",
            summary = "History of ingestion",
            description =
                    "수집 히스토리 API",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수집 히스토리 정보",
                            content = @Content(
                                    mediaType = "application/json",
                                    schemaProperties = {
                                            @SchemaProperty(name = "totalSize",
                                                    schema = @Schema(implementation = Long.class)),
                                            @SchemaProperty(name = "data",
                                                    array = @ArraySchema(
                                                            schema = @Schema(implementation = CommonResponseDto.class)))
                                    })
                    )
            })
    @CommonResponse
    @GetMapping("/ingestionHistory")
    public Object ingestionHistory(
            @Parameter(description = "평균 응답 시간의 내림차순 혹은 오름차순을 정하기 위한 매개변수",
                    schema = @Schema(type = "boolean", example = "true"))
            @RequestParam(value = "orderByAsc", required = false,
                    defaultValue = "false") boolean orderBy,
            @RequestParam(value = "pageNumber", required = false,
                    defaultValue = "${pageable-config.ingestion-history.page_number}") @Min(0) int pageNumber,
            @Parameter(description = "한 페이지에 표시할 데이터의 수를 나타내는 매개변수",
                    schema = @Schema(type = "int", example = "30"))
            @RequestParam(value = "pageSize", required = false,
                    defaultValue = "${pageable-config.ingestion-history.page_size}") @Min(1) int pageSize
    ) {
        final PageRequest pageRequest = PageRequest.of(
                pageNumber,
                pageSize,
                orderBy ? Sort.by("eventAt").ascending() : Sort.by("eventAt").descending());

        return ingestionHistoryService.getIngestionHistory(pageRequest);
    }
}
