plugins {
    id("com.mobigen.java-application")
}

group = "${group}.services"

dependencies {
    implementation("com.mobigen.datafabric.share:models")
    // Apache Tika
    implementation("org.apache.tika:tika-core:2.9.1")
    // Postgres SQL
    implementation("org.postgresql:postgresql:42.6.0")
    // MariaDB
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.2")
    // for JsonObject
    implementation("com.google.code.gson:gson:2.10.1")
    // libs
    implementation("com.mobigen.datafabric.libs:rabbitmq")
}

application {
    mainClass.set("com.mobigen.datafabric.extraction.ExtractionApplication") // <1>
}