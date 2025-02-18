package com.mobigen.vdap.server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Open VDAP Server",
                version = "0.1.0-alpha",
                description = "Datafabric Tech Server",
                license = @License(name = "Apache 2.0")
        )
)
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
