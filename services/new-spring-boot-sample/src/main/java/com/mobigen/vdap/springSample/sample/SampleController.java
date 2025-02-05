package com.mobigen.vdap.springSample.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SampleController {

    final SampleService service;

    public SampleController(SampleService service) {
        this.service = service;
    }

    @GetMapping("/sample")
    public String getSample() {
        log.error("controller : get sample");
        return service.getSample();
    }
}
