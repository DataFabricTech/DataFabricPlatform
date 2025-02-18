import java.net.URI

plugins {
    id("com.mobigen.spring-boot-application")
}

group = "${group}.services"

dependencies {
    annotationProcessor(platform("com.mobigen.platform:product-platform"))
    implementation(platform("com.mobigen.platform:product-platform"))
    developmentOnly(platform("com.mobigen.platform:product-platform"))
    testImplementation(platform("com.mobigen.platform:test-platform"))
    // Open VDAP Schema
    implementation("com.mobigen.vdap.share:schema")
    // Common
    implementation("com.mobigen.vdap.libs:common")

    // Lombok (Optional, for reducing boilerplate code)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    // Spring Boot DevTools (Optional, for hot reloading)
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    // For Log
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")
    // OpenTelemetry
    implementation(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.12.0"))
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")

//    implementation("jakarta.json:jakarta.json-api:2.1.3")
//    implementation("org.apache.camel:camel-json-patch:4.9.0")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:4.0.0")

    // AOP
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // MySQL Driver
    implementation("com.mysql:mysql-connector-j")
    // Flyway Database Migration
    implementation("org.flywaydb:flyway-core")
    // MyBatis
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter")

    // OpenApi
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    
    // Test Dependencies
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test")
}