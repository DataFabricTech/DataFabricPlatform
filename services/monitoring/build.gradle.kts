plugins {
    id("java")
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.mobigen"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // spring boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    // validate
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-oracle")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.flywaydb:flyway-mysql")
    implementation("org.flywaydb:flyway-sqlserver")

    // db driver
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    runtimeOnly("org.postgresql:postgresql")
    implementation("io.minio:minio:8.5.7")

    // k8s
    val kubernetesVersion = "18.0.0"
    implementation("io.kubernetes:client-java:$kubernetesVersion")
    implementation("io.kubernetes:client-java-api:$kubernetesVersion")
    implementation("io.kubernetes:client-java-api-fluent:$kubernetesVersion")
    implementation("io.kubernetes:client-java-extended:$kubernetesVersion")

    // gson
    implementation("com.google.code.gson:gson:2.10.1")

    // yaml to code
    implementation("net.rakugakibox.util:yaml-resource-bundle:1.2")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.register<JavaExec>("validate") {
    group = "application"
    description = "Run YamlToEnum Java class to generate enums from YAML file."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mobigen.monitoring.builder.YamlToEnum")
}

tasks.named("check") {
    dependsOn("validate")
}
