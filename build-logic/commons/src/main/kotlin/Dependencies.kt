object Dependencies {
    const val LOMBOK = "org.projectlombok:lombok:${Versions.LOMBOK_VER}"

    object Log4j {
        const val API           = "org.apache.logging.log4j:log4j-api:${Versions.LOG4J_VER}"
        const val CORE          = "org.apache.logging.log4j:log4j-core:${Versions.LOG4J_VER}"
        const val SLF4J_IMPL    = "org.apache.logging.log4j:log4j-slf4j-impl:${Versions.LOG4J_VER}"
        const val JACKSON_YAML  = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Versions.JACKSON_YAML_VER}"
    }

    object Protobuf {
        const val PROTOC            = "com.google.protobuf:protoc:${Versions.PROTOC_VER}"
        const val PROTOC_GEN_JAVA   = "io.grpc:protoc-gen-grpc-java:${Versions.GRPC_VER}"
        const val PROTOBUF_UTIL     = "com.google.protobuf:protobuf-java-util:${Versions.PROTOC_VER}"
    }

    object GRPC {
        const val API       = "io.grpc:grpc-api:${Versions.GRPC_VER}"
        const val STUB      = "io.grpc:grpc-stub:${Versions.GRPC_VER}"
        const val PROTOBUF  = "io.grpc:grpc-protobuf:${Versions.GRPC_VER}"
        const val SERVICE   = "io.grpc:grpc-services:${Versions.GRPC_VER}"
        const val NETTY     = "io.grpc:grpc-netty:${Versions.GRPC_VER}"
        const val TESTING   = "io.grpc:grpc-testing:${Versions.GRPC_VER}"
    }

    object JUNIT {
        const val BOM       = "org.junit:junit-bom:${Versions.JUNIT_VER}"
        const val JUPITER   = "org.junit.jupiter:junit-jupiter:${Versions.JUNIT_VER}"
        const val PLATFORM_LAUNCH = "org.junit.platform:junit-platform-launcher:${Versions.JUNIT_PLATFORM_LAUNCH}"
        const val JUPITER_ENGINE = "org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_VER}"
        const val MOCKITO   = "org.mockito:mockito-core:${Versions.MOCKITO_VER}"
        const val MOCKITO_JUPITER   = "org.mockito:mockito-junit-jupiter:${Versions.MOCKITO_VER}"
    }

    object Spring {
        const val CONFIG_PROC = "org.springframework.boot:spring-boot-configuration-processor:${Versions.SPRING_BOOT_VER}"
        const val BOOT = "org.springframework.boot:spring-boot:${Versions.SPRING_BOOT_VER}"
        const val BOOT_STARTER = "org.springframework.boot:spring-boot-starter:${Versions.SPRING_BOOT_VER}"
        const val STARTER_WEB = "org.springframework.boot:spring-boot-starter-web:${Versions.SPRING_BOOT_VER}"
        const val JPA = "org.springframework.boot:spring-boot-starter-data-jpa:${Versions.SPRING_BOOT_VER}"
        const val VALIDATION = "org.springframework.boot:spring-boot-starter-validation:${Versions.SPRING_BOOT_VER}"
        const val TEST = "org.springframework.boot:spring-boot-starter-test:${Versions.SPRING_BOOT_VER}"
    }

    object RabbitMQ {
        const val AMQP_CLIENT = "com.rabbitmq:amqp-client:${Versions.RABBITMQ_VER}"
    }
}
