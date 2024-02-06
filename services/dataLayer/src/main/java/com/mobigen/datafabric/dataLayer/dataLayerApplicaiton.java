package com.mobigen.datafabric.dataLayer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("dto")
public class dataLayerApplicaiton {
    public static void main(String[] args) {
        SpringApplication.run(dataLayerApplicaiton.class, args);
    }
}
