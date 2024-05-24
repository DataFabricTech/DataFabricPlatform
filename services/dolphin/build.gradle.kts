plugins {
    antlr
    id("com.mobigen.java-library")
    id("com.mobigen.java-application")
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.4"
}

allprojects {
    group = "${group}.dolphin"
    version = "1.0-SNAPSHOT"
}

repositories {
    mavenCentral()
}

configurations {
    all {
        exclude(module = "spring-boot-starter-logging")
    }
}

dependencies {
    // antlr
    antlr("org.antlr:antlr4:4.13.1")
    compileOnly("org.antlr:antlr4-runtime:4.13.1")

    // api
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("io.springfox:springfox-boot-starter:3.0.0")

    // db
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // - local
    implementation("mysql:mysql-connector-java:8.0.33")

    // - trino
    implementation("io.trino:trino-jdbc:444")
    compileOnly("io.trino:trino-spi:448")
    implementation("io.opentelemetry:opentelemetry-semconv:1.30.1-alpha")
    implementation("io.opentelemetry:opentelemetry-api:1.38.0")

    // - open metadata

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
