plugins {
    id("com.mobigen.java-library")
}

// define group = com.mobigen.vdap < in com.mobigen.common build.gradle.kts
group = "${group}.libs"
version = "1.0.0"

dependencies {
    // 의존성 버전 관리를 위해 각 프로세싱 단계를 platform 을 이용해 처리.
    // 버전 정보는 platforms/product-platform 에 선언되어 있음.
    annotationProcessor(platform("com.mobigen.platform:product-platform"))
    implementation(platform("com.mobigen.platform:product-platform"))

    // Lombok
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    // For Log
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")
}