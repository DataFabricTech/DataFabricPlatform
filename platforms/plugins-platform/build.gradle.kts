plugins {
    id("java-platform")
}

group = "com.mobigen.platform"

dependencies {
    constraints {
        api("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.1.0")
        api("org.springframework.boot:org.springframework.boot.gradle.plugin:3.4.0")
    }
}
