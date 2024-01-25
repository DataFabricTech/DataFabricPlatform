plugins {
    id("com.mobigen.spring-boot-application")
}

group = "${group}.services"

dependencies {
    // postgres
//    implementation("org.postgresql:postgresql:42.6.0")
    // Plugin
//    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:3.1.4")
//    implementation("org.springframework.boot:spring-boot-dependencies:3.1.4")
}

//tasks.withType<Test>().configureEach {
//    val outputDir = reports.junitXml.outputLocation
//    jvmArgumentProviders += CommandLineArgumentProvider {
//        listOf(
//            "-Djunit.platform.reporting.open.xml.enabled=true",
//            "-Djunit.platform.reporting.output.dir=${outputDir.get().asFile.absolutePath}"
//        )
//    }
//}

//tasks.jacocoTestReport {
//    dependsOn(tasks.test)
//    enabled = true
//    reports {
//        xml.required = true
//        html.required = true
//        csv.required = true
//    }
//}