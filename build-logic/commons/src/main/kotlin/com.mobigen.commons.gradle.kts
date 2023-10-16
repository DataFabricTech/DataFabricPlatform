plugins {
    id("java")
    id("com.mobigen.jacoco")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    constraints {
        implementation("org.apache.commons:commons-text:1.10.0") // <3>
    }
    // For Annotation
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.projectlombok:lombok:1.18.30")

    // For Log
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
    // For YAML Format Configurations - For To Using Log4j2
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")

    // For Test
    // JUnit Jupiter = 테스트 작성용
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3") // <4>
    // JUnit Platform Launcher = 테스트를 검색, 필터링 및 실행하는 데 사용할 수 있음(Gradle Test Task 상에서 Filter 적용 등)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform() // <5>

    testLogging {
        events("passed", "skipped", "failed") // <6>
    }
}
