plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(project(":commons"))
    // Version 정보를 외부에서 주입하는 형태로 만들고 싶지만.. 구성하지 못함.
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:3.1.4")
}
