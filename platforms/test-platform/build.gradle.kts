plugins {
    id("java-platform")
}

group = "com.mobigen.platform"

// allow the definition of dependencies to other platforms like the JUnit 5 BOM
javaPlatform.allowDependencies()

dependencies {
    // junit5
    api(platform("org.junit:junit-bom:5.11.3"))
    api(platform("org.mockito:mockito-bom:5.14.2"))
    constraints {
        api("org.springframework.boot:spring-boot-starter-test:3.4.0")
        api("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.4")
    }
}
