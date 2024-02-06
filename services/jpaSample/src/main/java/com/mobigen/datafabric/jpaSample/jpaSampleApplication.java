package com.mobigen.datafabric.jpaSample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan("com.mobigen")
public class jpaSampleApplication {
    public static void main(String[] args) {
        log.debug("Data Layer Main Start");
    }
}