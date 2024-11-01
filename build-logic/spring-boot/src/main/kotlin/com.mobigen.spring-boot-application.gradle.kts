plugins {
    id("com.mobigen.commons")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    annotationProcessor(Dependencies.Spring.CONFIG_PROC)

    implementation(Dependencies.Spring.BOOT)
    implementation(Dependencies.Spring.BOOT_STARTER)
    implementation(Dependencies.Spring.STARTER_WEB)
    implementation(Dependencies.Spring.JPA)
    implementation(Dependencies.Spring.VALIDATION)

    testImplementation(Dependencies.Spring.TEST)
}
