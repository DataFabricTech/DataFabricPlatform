plugins {
    id("java-platform")
}

group = "com.mobigen.platform"

// allow the definition of dependencies to other platforms like the Spring Boot BOM
javaPlatform.allowDependencies()

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.4.0"))
    api(platform("org.apache.logging.log4j:log4j-bom:2.24.2"))
    api(platform("com.fasterxml.jackson:jackson-bom:2.18.1"))
    constraints {
        api("org.projectlombok:lombok:1.18.30")
    }
}
