package com.mobigen.datafabric.springSample.sample;

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
}
