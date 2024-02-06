plugins {
    id("com.mobigen.java-library")
}

group = "${group}.services"

dependencies {
    // DTO Model Class
    implementation("com.mobigen.datafabric.share:models")

    // openSearch
    implementation("org.opensearch.client:opensearch-java:2.6.0")
    implementation("org.apache.httpcomponents.core5:httpcore5:5.2.2")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")

    // sql parser - 추후에 지워도 되는 항목
    implementation("com.github.jsqlparser:jsqlparser:4.7")

    // postgres
    implementation("org.postgresql:postgresql:42.6.0")

    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa
    implementation(Dependencies.Spring.JPA)
    implementation(Dependencies.Spring.TEST)

    // For Test
    // https://mvnrepository.com/artifact/com.h2database/h2
    testImplementation("com.h2database:h2:2.2.224")
}