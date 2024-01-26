plugins {
    id("com.mobigen.java-application")
}

group = "${group}.services"

// Add Nexus Repository
repositories {
    maven {
        url = uri("http://nexus.iris.tools/repository/iris-framework-release/")
    }
}

dependencies {
    // For Configuration
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.0")
    // For Mobigen Configuration Framework

    implementation("com.mobigen.datafabric.libs:grpc")

    // jdbc
    implementation("org.postgresql:postgresql:42.6.0")
}

application {
    mainClass.set("com.mobigen.datafabric.core.Main") // <1>
}

sourceSets {
    main {

    }
}

tasks.withType<Jar> {
    tasks.named("shadowJar", ShadowJar::class) {
        mergeServiceFiles()
    }
    manifest {
        attributes["Main-Class"] = "com.mobigen.datafabric.core.Main"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
