package com.mobigen.datafabric.springSample.sample;

import org.springframework.stereotype.Service;

@Service
public class SampleService {
    public String getSample() {
        return "Hello World!";
    }
}
