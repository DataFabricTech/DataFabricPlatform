package com.mobigen.datafabric.relationship.trigger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    final SampleService service;

    public SampleController(SampleService service) {
        this.service = service;
    }

    @GetMapping("/sample")
    public String getSample() {
        return service.getSample();
    }

    @GetMapping("/get-table")
    public String getTable() {
        return service.testGetTableData();
    }

    @GetMapping("/get-fusion")
    public String getFusion() {
        return service.testGetFusionData();
    }
}
