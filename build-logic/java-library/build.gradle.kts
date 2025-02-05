plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform("com.mobigen.platform:plugins-platform"))

    implementation(project(":commons"))
}
