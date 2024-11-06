plugins {
    id("com.mobigen.java-library")
}

group = "${group}.share"
version = "0.0.2"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:${Versions.SPRING_BOOT_VER}")
}