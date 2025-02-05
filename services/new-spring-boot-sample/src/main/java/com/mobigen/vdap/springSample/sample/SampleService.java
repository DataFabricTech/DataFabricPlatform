package com.mobigen.vdap.springSample.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SampleService {
    public String getSample() {
        log.info("service : get sample");
        return "Hello World!";
    }
}
