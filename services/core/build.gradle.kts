import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.mobigen.java-application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.mobigen.datafabric"

val protobufVersion = "3.24.3"

dependencies {
    implementation("com.mobigen.datafabric.libs:grpc")
    implementation("com.mobigen.datafabric.libs:sqlgen")
    implementation("com.mobigen.datafabric.libs:configuration")

    // jdbc
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.apache.commons:commons-text:1.10.0")
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
