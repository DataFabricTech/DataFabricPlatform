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

dependencies {
    // For Annotation
    compileOnly(Dependency.lombok)
    annotationProcessor(Dependency.lombok)

    // For Log
    implementation(Dependency.log4j.log4jApi)
    implementation(Dependency.log4j.log4jCore)
    implementation(Dependency.log4j.log4jSlf4jImpl)
    // For YAML Format Configurations - For To Using Log4j2
    implementation(Dependency.log4j.jacksonYaml)

    // For Test
    // JUnit Jupiter = 테스트 작성용
    testImplementation(Dependency.junitTest.junitJupiter)
    // JUnit Platform Launcher = 테스트를 검색, 필터링 및 실행하는 데 사용할 수 있음(Gradle Test Task 상에서 Filter 적용 등)
    testRuntimeOnly(Dependency.junitTest.junitPlatformLauncher)
    testImplementation(Dependency.junitTest.mockito)
}

tasks.named<Test>("test") {
    useJUnitPlatform() // <5>

    testLogging {
        events("passed", "skipped", "failed") // <6>
    }
}
