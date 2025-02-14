plugins {
    id("com.mobigen.spring-boot-application")
}

group = "${group}.services"

dependencies {
    annotationProcessor(platform("com.mobigen.platform:product-platform"))
    implementation(platform("com.mobigen.platform:product-platform"))
    developmentOnly(platform("com.mobigen.platform:product-platform"))
    // Open VDAP Schema
    implementation("com.mobigen.vdap.share:schema")

    // Lombok (Optional, for reducing boilerplate code)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    // For Log
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")
    // OpenTelemetry
    implementation(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.12.0"))
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    // MySQL Driver
    implementation("com.mysql:mysql-connector-j")
    // Flyway Database Migration
    implementation("org.flywaydb:flyway-core")
    // Spring Boot DevTools (Optional, for hot reloading)
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    // OpenApi
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3")
    // Test Dependencies
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test")
}