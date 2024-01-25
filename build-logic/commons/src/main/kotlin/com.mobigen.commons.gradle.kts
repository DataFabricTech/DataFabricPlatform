plugins {
    id("java")
    id("com.mobigen.jacoco")
    idea
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "com.mobigen.datafabric"

dependencies {
    // For Annotation
    compileOnly(Dependencies.LOMBOK)
    annotationProcessor(Dependencies.LOMBOK)
    // For Unit Test Code Annotation
    testAnnotationProcessor(Dependencies.LOMBOK)
    testImplementation(Dependencies.LOMBOK)

    // For Log
    implementation(Dependencies.Log4j.API)
    implementation(Dependencies.Log4j.CORE)
    implementation(Dependencies.Log4j.SLF4J_IMPL)
    // For YAML Format Configurations - For To Using Log4j2
    implementation(Dependencies.Log4j.JACKSON_YAML)

    // For Test
    testImplementation(platform(Dependencies.JUNIT.BOM))
    testImplementation(Dependencies.JUNIT.JUPITER)
    testRuntimeOnly(Dependencies.JUNIT.PLATFORM_LAUNCH)
    testRuntimeOnly(Dependencies.JUNIT.JUPITER_ENGINE)
//    testRuntimeOnly("org.junit.platform:junit-platform-reporting:1.10.1")
    testImplementation(Dependencies.JUNIT.MOCKITO)
}

tasks.named<Test>("test") {
    useJUnitPlatform() // <5>

    testLogging {
        events("passed", "skipped", "failed") // <6>
    }
}
