plugins {
    id("com.mobigen.spring-boot-application")
}

group = "${group}.services"

dependencyManagement {
    imports {
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.6.0")
    }
}

dependencies {
    // openapi ui - swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // jaeger
    // https://opentelemetry.io/docs/zero-code/java/spring-boot-starter/getting-started/
    // https://www.jaegertracing.io/docs/1.59/deployment/
    // https://opentelemetry.io/docs/languages/sdk-configuration/general/
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    implementation("io.opentelemetry:opentelemetry-exporter-jaeger:1.34.1")

    implementation(files("../../external-libs/openmetadata-spec-1.4.0.jar"))

    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:${Versions.SPRING_BOOT_VER}")
    // DataSource Driver: MySQL, Postgresql
    implementation("com.mysql:mysql-connector-j:${Versions.MYSQL}")
    implementation("org.postgresql:postgresql:${Versions.POSTGRESQL}")

    // For FullyQualifiedClassName
    implementation("commons-codec:commons-codec:1.17.0")

    // For MinIO
    implementation("io.minio:minio:8.5.13")

    // Model
//    implementation("com.mobigen.datafabric.share:models")
}

//tasks.withType<Test>().configureEach {
//    val outputDir = reports.junitXml.outputLocation
//    jvmArgumentProviders += CommandLineArgumentProvider {
//        listOf(
//            "-Djunit.platform.reporting.open.xml.enabled=true",
//            "-Djunit.platform.reporting.output.dir=${outputDir.get().asFile.absolutePath}"
//        )
//    }
//}

//tasks.jacocoTestReport {
//    dependsOn(tasks.test)
//    enabled = true
//    reports {
//        xml.required = true
//        html.required = true
//        csv.required = true
//    }
//}