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

    // for JsonObject
    implementation("com.google.code.gson:gson:2.10.1")

    // extraction
    implementation(project(":extraction"))

    // for GRPC Server
    implementation(Dependencies.GRPC.SERVER)

    // for Spring
    implementation(Dependencies.Spring.BOOT_STARTER)
    implementation(Dependencies.Spring.JPA)

    // Rabbit MQ
    implementation("com.rabbitmq:amqp-client:5.20.0")

    // DTO Model Class
    implementation("com.mobigen.datafabric.share:models")
    implementation(project(":dataLayer"))

    // share/interfaces : protobuf(gRPC)
    implementation("com.mobigen.datafabric.share:interfaces")

    // lib/rabbitmq
    implementation("com.mobigen.datafabric.libs:rabbitmq")
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
