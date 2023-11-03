plugins {
    id("com.mobigen.java-application")
}

group = "com.mobigen.datafabric"

val protobufVersion = "3.24.3"

dependencies {
    implementation("com.mobigen.libs:grpc")
    implementation("com.mobigen.libs:sqlgen")
    implementation("com.mobigen.libs:configuration")

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
