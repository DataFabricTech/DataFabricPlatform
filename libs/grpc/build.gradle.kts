plugins {
    id("com.mobigen.java-library")
    id("io.freefair.aspectj.post-compile-weaving") version "6.4.1"
}

group = "com.mobigen.libs"

val protobufVersion = "3.24.3"
val protocVersion = protobufVersion
val grpcVersion = "1.56.1"

dependencies {
    api("com.mobigen.share:models")
    api("org.aspectj:aspectjrt:1.9.8")
    api("org.aspectj:aspectjweaver:1.9.8")
//    implementation("org.codehaus.mojo:aspectj-maven-plugin:1.8")
    // gRPC
    api("io.grpc:grpc-stub:${grpcVersion}")
    api("io.grpc:grpc-protobuf:${grpcVersion}")
    api("io.grpc:grpc-services:${grpcVersion}")
    api("io.grpc:grpc-netty:${grpcVersion}")
    api("io.grpc:grpc-testing:${grpcVersion}")
    api("io.perfmark:perfmark-api:0.26.0")

    if (JavaVersion.current().isJava9Compatible()) {
        api("javax.annotation:javax.annotation-api:1.3.2")
    }

    // protobuf
    api("com.google.protobuf:protobuf-java-util:${protobufVersion}")
}

// 23.11.03 - protobuf task(proto는 share에서 빌드) 삭제 - jblim