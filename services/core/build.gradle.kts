plugins {
    id("com.mobigen.java-application")
}

group = "com.mobigen.datafabric"

val protobufVersion = "3.24.3"

dependencies {
    implementation("com.mobigen.libs:grpc")
    implementation("com.mobigen.libs:sqlgen")

    // jdbc
    implementation("org.postgresql:postgresql:42.6.0")

    // the GOAT ORM
    implementation("org.hibernate.orm:hibernate-core:6.3.0.Final")
    // Hibernate Validator
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("org.glassfish:jakarta.el:4.0.2")

    // jdbc
    implementation("org.postgresql:postgresql:42.6.0")

// protobuf
//    implementation("com.google.protobuf:protobuf-java-util:${protobufVersion}")
//    instrumentedClasspath(project(mapOf("com.mobigen.libs:grpc"), path" to ":producer",
//        "configuration" to "instrumentedJars")))
}

application {
    mainClass.set("com.mobigen.datafabric.core.Main") // <1>
}

sourceSets {
    main {

    }
}
