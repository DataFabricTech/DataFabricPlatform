package com.mobigen.monitoring.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("My API Documentation")
                        .version("1.0")
                        .description("API documentation using springdoc-openapi and Swagger UI"));
    }
//
//    @Bean
//    public GroupedOpenApi publicApi() {
//        return GroupedOpenApi.builder()
//                .group("v1")
//                .pathsToMatch("/api/v1/**") // 특정 경로만 API 문서에 포함
//                .build();
//    }
}