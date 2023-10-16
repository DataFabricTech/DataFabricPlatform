
plugins {
    id("com.mobigen.java-application")
}

group = "com.mobigen.datafabric"

dependencies {
    implementation("org.apache.commons:commons-text")
    implementation("com.mobigen.utilities:list")
}

application {
    mainClass.set("com.mobigen.datafabric.sample.Sample") // <1>
}