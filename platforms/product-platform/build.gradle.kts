plugins {
    id("java-platform")
}

group = "com.mobigen.platform"

// allow the definition of dependencies to other platforms like the Spring Boot BOM
javaPlatform.allowDependencies()

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.4.0"))
    api(platform("org.springframework.boot:spring-boot-dependencies:3.4.0"))
    api(platform("org.apache.logging.log4j:log4j-bom:2.24.2"))
    api(platform("com.fasterxml.jackson:jackson-bom:2.18.1"))
    constraints {
        api("org.projectlombok:lombok:1.18.30")
        // For JSONSchema2Pojo + Annotation
        api("org.jsonschema2pojo:jsonschema2pojo-core:1.2.2")
        api("org.glassfish.jaxb:codemodel:4.0.5")
        api("jakarta.validation:jakarta.validation-api:3.1.0")

        // MySQL
        api("com.mysql:mysql-connector-j:9.1.0")
        // flyway
        api("org.flywaydb:flyway-core:11.0.1")
        api("org.flywaydb:flyway-mysql:11.0.1")
        // MyBatis
        api("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4")
        // SpringBoot DevTools
        api("org.springframework.boot:spring-boot-devtools:3.4.0")
        // RestAPI - Swagger Document
        api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3")

        // JSON
        api("jakarta.json:jakarta.json-api:2.1.3")
        api("org.glassfish:jakarta.json:2.0.1")
        api("com.github.java-json-tools:json-patch:1.13")
    }
}
