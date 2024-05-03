plugins {
    antlr
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation(project(":utils"))

    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")

    antlr("org.antlr:antlr4:4.11.1")
    implementation("org.antlr:antlr4-runtime:4.11.1")
    implementation("org.json:json:20231013")
    runtimeOnly("com.h2database:h2")

    implementation("org.apache.parquet:parquet-avro:1.13.1")
    implementation("org.apache.hadoop:hadoop-common:3.3.6")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.657")
    implementation("com.amazonaws:aws-java-sdk:1.12.657")
    implementation("org.apache.hadoop:hadoop-aws:3.3.6")
}
sourceSets {
    main {
        java {
            srcDir(tasks.generateGrammarSource)
        }
    }
}
tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages", "-package", "com.mobigen.dolphin")
    outputDirectory = file("${project.buildDir}/generated/sources/antlr")
}
