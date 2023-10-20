object Dependency {
    const val lombok = "org.projectlombok:lombok:${Versions.lombokVer}"

    object log4j {
        const val log4jApi = "org.apache.logging.log4j:log4j-api:${Versions.log4jVer}"
        const val log4jCore = "org.apache.logging.log4j:log4j-core:${Versions.log4jVer}"
        const val log4jSlf4jImpl = "org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4jVer}"
        const val jacksonYaml = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Versions.jacksonYamlVer}"
    }

    object protobuf {
        const val protoc = "com.google.protobuf:protoc:${Versions.protocVer}"
        const val protocGenJava = "io.grpc:protoc-gen-grpc-java:${Versions.grpcVer}"
        const val protoBufUtil = "com.google.protobuf:protobuf-java-util:${Versions.protocVer}"
    }

    object grpc {
        const val api = "io.grpc:grpc-api:${Versions.grpcVer}"
        const val stub = "io.grpc:grpc-stub:${Versions.grpcVer}"
        const val protobuf = "io.grpc:grpc-protobuf:${Versions.grpcVer}"
        const val service = "io.grpc:grpc-services:${Versions.grpcVer}"
        const val netty = "io.grpc:grpc-netty:${Versions.grpcVer}"
        const val testing = "io.grpc:grpc-testing:${Versions.grpcVer}"
    }

    object junitTest {
        const val junitJupiter            = "org.junit.jupiter:junit-jupiter:${Versions.junitVer}"
        const val junitPlatformLauncher   = "org.junit.platform:junit-platform-launcher:${Versions.junitPlatformVer}"
    }

    const val springGradlePlugin = "org.springframework.boot:org.springframework.boot.gradle.plugin:${Versions.springGradlePluginVer}"
}
