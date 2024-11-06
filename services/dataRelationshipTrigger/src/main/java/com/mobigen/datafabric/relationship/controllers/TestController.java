package com.mobigen.datafabric.relationship.controllers;

import com.mobigen.datafabric.relationship.services.DataCollector;
import com.mobigen.datafabric.relationship.trigger.SampleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    final DataCollector collectorService;

    public TestController(DataCollector collectorService) {
        this.collectorService = collectorService;
    }

    @GetMapping("/tableDataCollect")
    public String getTableData() {
        collectorService.collectData();
        return "table data collect";
    }
}
