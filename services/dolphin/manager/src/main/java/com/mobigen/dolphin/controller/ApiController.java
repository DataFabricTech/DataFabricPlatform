package com.mobigen.dolphin.controller;

import com.mobigen.dolphin.model.request.ExecuteDto;
import com.mobigen.dolphin.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dolphin/v1")
public class ApiController {
    private final QueryService queryService;
    private final String aa = UUID.randomUUID().toString();

    @PostMapping("/query/execute")
    public Object execute(@RequestBody ExecuteDto executeDto) {
        return queryService.execute(executeDto.getQuery());
    }

    @GetMapping("/query/read/{job_id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
    public Object read(@PathVariable("job_id") String jobId) {
        return null;
    }

    @GetMapping("/query/download/{job_id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
    public Object download(@PathVariable("job_id") String jobId) {
        return null;
    }

    @GetMapping("/query/status/{job_id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
    public Object status(@PathVariable("job_id") String jobId) {
        return queryService.status(UUID.fromString(jobId));
    }
}
