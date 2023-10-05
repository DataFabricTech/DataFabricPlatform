import com.google.protobuf.gradle.*

plugins {
    id("com.mobigen.java-library")
    // Protobuf Gradle Plugin
    id("com.google.protobuf") version "0.9.4"
    idea
}

group = "com.mobigen.libs"

val protobufVersion = "3.24.3"
val protocVersion = protobufVersion
val grpcVersion = "1.56.1"

dependencies {
    // gRPC
    api("io.grpc:grpc-stub:${grpcVersion}")
    api("io.grpc:grpc-protobuf:${grpcVersion}")
    api("io.grpc:grpc-services:${grpcVersion}")
    api("io.grpc:grpc-netty:${grpcVersion}")
    api("io.grpc:grpc-testing:${grpcVersion}")

    if (JavaVersion.current().isJava9Compatible()) {
        api("javax.annotation:javax.annotation-api:1.3.2")
    }

    // protobuf
    api("com.google.protobuf:protobuf-java-util:${protobufVersion}")

    // Extra proto source files besides the ones residing under "src/main".
    // protobuf(files("proto/"))
}

protobuf {
    // The artifact spec for the Protobuf Compiler
    protoc {
//        if (osdetector.os == "osx") {
//            if( osdetector.arch == "aarch_64") {
//                artifact = "com.google.protobuf:protoc:${protocVersion}:osx-aarch_64"
//            } else {
//                artifact = "com.google.protobuf:protoc:${protocVersion}:osx-x86_64"
//            }
//        } else {
            artifact = "com.google.protobuf:protoc:${protocVersion}"
//        }
    }
    plugins {
        id("grpc") {
//            if (osdetector.os == "osx") {
//                if( osdetector.arch == "aarch_64") {
//                    artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}:osx-aarch_64"
//                } else {
//                    artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}:osx-x86_64"
//                }
//            } else {
                artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
//            }
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without
                // options. Note the braces cannot be omitted, otherwise the
                // plugin will not be added. This is because of the implicit way
                // NamedDomainObjectContainer binds the methods.
                id("grpc") { }
            }
        }
    }
}

// IntelliJ 인식 용
idea {
    module {
        generatedSourceDirs.addAll(listOf(
            file("build/generated/source/main/grpc"),
            file("build/generated/source/main/java"),
        ))
    }
}

// 타 모듈(서비스)에서 gRPC Generated Source Directory 인식을 위해 추가
sourceSets{
    main {
        java {
            srcDirs("build/generated/source/main/java")
        }
//        resources {
//            srcDirs("src/main/resources", "build/generated/source/main/resources")
//        }
    }
}