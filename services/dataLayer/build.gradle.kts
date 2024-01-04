plugins {
    id("com.mobigen.java-application")
}

group = "com.mobigen.datafabric"

val protobufVersion = "3.24.3"

dependencies {
    implementation("com.mobigen.datafabric.libs:grpc")
    implementation("com.mobigen.datafabric.libs:configuration")

    // protobuf
    implementation("com.google.protobuf:protobuf-java-util:${protobufVersion}")
    //    instrumentedClasspath(project(mapOf("com.mobigen.libs:grpc"), path" to ":producer",
    //        "configuration" to "instrumentedJars")))

    // openSearch
    implementation("org.opensearch.client:opensearch-java:2.6.0")
    implementation("org.apache.httpcomponents.core5:httpcore5:5.2.2")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")

    // sql parser
    implementation("com.github.jsqlparser:jsqlparser:4.7")

    // postgres
    implementation("org.postgresql:postgresql:42.6.0")

    // For Test
    // JUnit Jupiter = 테스트 작성용
//    testImplementation("org.junit.jupiter:junit-jupiter") // <4>
}

application {
    mainClass.set("com.mobigen.datafabric.dataLayer.DataLayerApplication") // <1>
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.mobigen.datafabric.dataLayer.DataLayerApplication"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
