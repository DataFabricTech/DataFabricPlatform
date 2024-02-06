plugins {
    id("com.mobigen.java-application")
    id("com.mobigen.java-library")
}

group = "${group}.services"

dependencies {
    // DTO Model Class
    implementation("com.mobigen.datafabric.share:models")
    implementation(project(":dataLayer"))

    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa
    implementation(Dependencies.Spring.BOOT)
    implementation(Dependencies.Spring.JPA)
    implementation(Dependencies.Spring.TEST)


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
