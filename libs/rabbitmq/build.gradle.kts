plugins {
    id("com.mobigen.java-library")
}

group = "${group}.libs"

dependencies {
    // For Message Queue(RabbitMQ)
    api(Dependencies.RabbitMQ.AMQP_CLIENT)

    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
}