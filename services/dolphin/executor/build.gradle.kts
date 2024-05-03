dependencies {
    implementation("org.apache.spark:spark-core_2.12:3.5.0")
    implementation("org.apache.spark:spark-hive_2.12:3.5.0")

    implementation("com.crealytics:spark-excel_2.12:3.4.1_0.20.3")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.apache.hadoop:hadoop-aws:3.3.4")
    compileOnly("org.slf4j:slf4j-api:1.7.36")
    compileOnly("org.apache.spark:spark-sql_2.12:3.5.0")

    implementation("org.springframework:spring-beans")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("io.trino:trino-jdbc:439")
    compileOnly("io.trino:trino-spi:439")
    implementation("io.opentelemetry:opentelemetry-semconv:0.14.0")
    implementation("io.opentelemetry:opentelemetry-api:0.14.0")

    implementation(project(":utils"))
    runtimeOnly("com.h2database:h2")
}
