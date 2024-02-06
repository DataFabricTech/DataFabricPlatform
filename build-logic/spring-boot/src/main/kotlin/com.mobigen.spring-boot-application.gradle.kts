plugins {
    id("com.mobigen.commons")
    id("org.springframework.boot")
}

dependencies {
    annotationProcessor(Dependencies.Spring.CONFIG_PROC)

    implementation(Dependencies.Spring.BOOT)
    implementation(Dependencies.Spring.BOOT_STARTER)
    implementation(Dependencies.Spring.STARTER_WEB)

    testImplementation(Dependencies.Spring.TEST)
}
