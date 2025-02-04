plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform("com.mobigen.platform:plugins-platform")) // <2>

    implementation(project(":commons"))

    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin")  // <4>
}
