//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.mobigen.java-application")
//    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "${group}.services"

dependencies {
    // for JDBC
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.apache.commons:commons-text:1.10.0")

    // share/interfaces : protobuf(gRPC)
    implementation("com.mobigen.datafabric.share:interfaces")
}

application {
    mainClass.set("com.mobigen.datafabric.core.Main") // <1>
}
//tasks.withType<Jar> {
//    tasks.named("shadowJar", ShadowJar::class) {
//        mergeServiceFiles()
//    }
//    manifest {
//        attributes["Main-Class"] = "com.mobigen.datafabric.core.Main"
//    }
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
//    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
//}
