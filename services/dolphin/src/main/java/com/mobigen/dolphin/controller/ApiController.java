package com.mobigen.dolphin.controller;

import com.mobigen.dolphin.dto.request.CreateModelDto;
import com.mobigen.dolphin.dto.request.ExecuteDto;
import com.mobigen.dolphin.dto.response.ModelDto;
import com.mobigen.dolphin.dto.response.QueryResultDTO;
import com.mobigen.dolphin.service.ModelService;
import com.mobigen.dolphin.service.QueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Tag(name = "Dolphin Main API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/dolphin/v1")
public class ApiController {
    private final QueryService queryService;
    private final ModelService modelService;

    @Operation(summary = "Get dataModels", description = "Returns list of dataModels")
    @GetMapping("/model")
    public List<ModelDto> getModels() {
        return modelService.getModels();
    }

    @Operation(summary = "Create dataModel", description = "Create a dataModel by method (MODEL, QUERY, CONNECTOR)")
    @PostMapping("/model")
    public ModelDto addModel(@RequestBody CreateModelDto createModelDto) {
        return modelService.createModel(createModelDto);
    }

    @Operation(summary = "Execute Query using DataModel")
    @PostMapping("/query/execute")
    public QueryResultDTO execute(@RequestBody ExecuteDto executeDto) {
        return queryService.execute(executeDto);
    }

    @Operation(summary = "Async Execute Query using DataModel")
    @PostMapping("/query/async/execute")
    public Object asyncExecute(@RequestBody ExecuteDto executeDto) {
        return queryService.executeAsync(executeDto);
    }

    @Operation(summary = "Read result data of asynchronous query using JobId")
    @GetMapping("/query/read/{job_id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
    public Object read(@PathVariable("job_id") UUID jobId) {
        return queryService.read(jobId);
    }

    @Operation(summary = "Download result data of asynchronous query using JobId")
    @GetMapping("/query/download/{job_id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
    public Object download(@PathVariable("job_id") UUID jobId) {
        return null;
    }

    @Operation(summary = "Check status of asynchronous query job using JobId")
    @GetMapping("/query/status/{job_id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
    public Object status(@PathVariable("job_id") UUID jobId) {
        return queryService.status(jobId);
    }
}
