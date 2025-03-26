plugins {
    id("com.mobigen.spring-boot-application")
}

group = "${group}.services"

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    // 버전 중앙 관리를 위한 각 Task 마다 platform 정보를 추가
    annotationProcessor(platform("com.mobigen.platform:product-platform"))
    testAnnotationProcessor(platform("com.mobigen.platform:product-platform"))
    implementation(platform("com.mobigen.platform:product-platform"))
    developmentOnly(platform("com.mobigen.platform:product-platform"))
    testImplementation(platform("com.mobigen.platform:test-platform"))
    mockitoAgent(platform("com.mobigen.platform:test-platform"))

    // Open VDAP Common Library
    implementation("com.mobigen.vdap.libs:common")
    // Open VDAP Schema
    implementation("com.mobigen.vdap.share:annotator")
    implementation("com.mobigen.vdap.share:schema")

    // Lombok (Optional, for reducing boilerplate code)
    compileOnly("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
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
    // Keycloak
    implementation("org.keycloak:keycloak-admin-client:26.0.4")

    // For Test : @Visiblefortesting
    implementation("com.google.guava:guava:33.4.0-jre")

    // AOP
    implementation("org.springframework.boot:spring-boot-starter-aop")
    // JDBC
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // MySQL Driver
    implementation("com.mysql:mysql-connector-j")
    // Flyway Database Migration
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    // For UUIDv7
    implementation("com.fasterxml.uuid:java-uuid-generator:5.1.0")
    // For JSON
    implementation("jakarta.json:jakarta.json-api:2.1.3")
    implementation("org.glassfish:jakarta.json:2.0.1")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.github.java-json-tools:json-patch:1.13")

    // OpenApi
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    
    // Test Dependencies
    // testImplementation("org.springframework.boot:spring-boot-starter-test")      // 이미 springboot 타입일 경우 추가되어 있음.
    // JUnit 5 (Jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    // Mock
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    // 여기서 String 으로 설정해야 함에 따라 버전 정보 기입
    mockitoAgent("org.mockito:mockito-core") {isTransitive = false}
    // TestContainer
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:mysql")
}

tasks.withType<Test> {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}