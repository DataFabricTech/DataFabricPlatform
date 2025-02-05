plugins {
    id("com.mobigen.commons")
    id("org.springframework.boot")
}

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation( "org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
