plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(project(":commons"))

    api("org.springframework.boot:org.springframework.boot.gradle.plugin:3.1.4")
}
