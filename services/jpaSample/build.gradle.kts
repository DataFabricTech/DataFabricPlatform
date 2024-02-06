plugins {
    id("com.mobigen.java-library")
    id("com.mobigen.java-application")
}

dependencies {
//    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa
    implementation(Dependencies.Spring.BOOT)
    implementation(Dependencies.Spring.JPA)
    implementation(Dependencies.Spring.TEST)

    implementation(project(":dataLayer"))

    // https://mvnrepository.com/artifact/com.h2database/h2
    testImplementation("com.h2database:h2:2.2.224")
}

application {
    mainClass.set("com.mobigen.datafabric.jpaSample.jpaSampleApplication") // <1>
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.mobigen.datafabric.jpaSample.jpaSampleApplication"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
