plugins {
    id("com.mobigen.spring-boot-application")
}

group = "${group}.services"

dependencies {
    annotationProcessor(platform("com.mobigen.platform:product-platform"))
    implementation(platform("com.mobigen.platform:product-platform"))
    // Lombok
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    // For Log
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")
    // SpringBoot

    // OpenTelemetry
    implementation(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.12.0"))
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
}