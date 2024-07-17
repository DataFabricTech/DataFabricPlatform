plugins {
    antlr
    id("com.mobigen.java-library")
    id("com.mobigen.java-application")
    id("org.springframework.boot") version "3.3.0"
}

apply(plugin = "io.spring.dependency-management")

allprojects {
    group = "${group}.dolphin"
    version = "1.0-SNAPSHOT"
}

repositories {
    mavenCentral()
}

configurations.forEach {
    it.exclude("org.springframework.boot", "spring-boot-starter-logging")
}

dependencies {
    // antlr
    antlr("org.antlr:antlr4:4.13.1")
    compileOnly("org.antlr:antlr4-runtime:4.13.1")

    // api
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // db
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // - local
    implementation("mysql:mysql-connector-java:8.0.33")

    // - trino
    implementation("io.trino:trino-jdbc:435")
    compileOnly("io.trino:trino-spi:435")
    implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.25.0-alpha")
    implementation("io.opentelemetry:opentelemetry-api:1.38.0")

    // - open metadata
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // json?
    implementation("org.json:json:20240303")
    runtimeOnly("com.h2database:h2")

    // parquet
//    implementation("org.apache.parquet:parquet-avro:1.13.1")
//    implementation("org.apache.hadoop:hadoop-common:3.3.6")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
    outputDirectory = file("${outputDirectory}/com/mobigen/dolphin/antlr")
}
