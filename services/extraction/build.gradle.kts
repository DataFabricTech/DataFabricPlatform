plugins {
    id("com.mobigen.java-application")
}

group = "${group}.services"

dependencies {
    // Apache Tika
    implementation("org.apache.tika:tika-core:2.9.1")
    // Postgres SQL
    implementation("org.postgresql:postgresql:42.6.0")
    // MariaDB
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.2")

    // For Test
    // JUnit Jupiter = 테스트 작성용
//    testImplementation("org.junit.jupiter:junit-jupiter") // <4>
}

application {
    mainClass.set("com.mobigen.datafabric.extraction.ExtractionApplication") // <1>
}

//tasks.withType<Jar> {
//    manifest {
//        attributes["Main-Class"] = "com.mobigen.datafabric.extraction.ExtractionApplication"
//    }
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
//}
