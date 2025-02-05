plugins {
    id("java")
    id("com.mobigen.jacoco")
    idea
}

group = "com.mobigen.vdap"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(platform("com.mobigen.platform:product-platform"))
    /*
    // For Annotation
    implementation("org.projectlombok:lombok:1.18.30")
    // For Log
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")
    // For YAML Format Configurations - For To Using Log4j2
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
     */

    // For Test
    testImplementation(platform("com.mobigen.platform:test-platform"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform() // <5>

    testLogging {
        events("passed", "skipped", "failed") // <6>
    }
}
