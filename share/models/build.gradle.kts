import com.google.protobuf.gradle.*

plugins {
    id("com.mobigen.java-library")
    id("com.google.protobuf") version "0.9.4"
}

group = "com.mobigen.share"
version = "0.0.1"

dependencies {
    // protobuf
    implementation(Dependencies.Protobuf.PROTOBUF_UTIL)
    // gRPC
    implementation(Dependencies.GRPC.STUB)
    implementation(Dependencies.GRPC.PROTOBUF)
    implementation(Dependencies.GRPC.SERVICE)
    implementation(Dependencies.GRPC.NETTY)
//    implementation(Dependencies.grpc.testing)

    if (JavaVersion.current().isJava9Compatible()) {
        implementation("javax.annotation:javax.annotation-api:1.3.2")
    }
}

protobuf {
    protoc {
        artifact = Dependencies.Protobuf.PROTOC
    }
    plugins {
        id("grpc") {
            artifact = Dependencies.Protobuf.PROTOC_GEN_JAVA
        }
    }
    generateProtoTasks {
//        ofSourceSet("main").forEach {
////            it.generateDescriptorSet = true
////            it.descriptorSetOptions.includeSourceInfo = true
////            it.descriptorSetOptions.includeImports = true
////            it.descriptorSetOptions.path = "${projectDir}/src/main/resources/${it.sourceSet.name}.pb"
//            it.plugins {
//                id("grpc") {
//                }
//            }
//        }
        ofSourceSet("main").forEach { task ->
            task.plugins {
                id("grpc") {
                }
            }
        }
    }
}

tasks.withType<Copy> {
    filesMatching("**/*.proto") {
        // import 로 인해 중복 발생하는 class의 처리
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

// 타 모듈(서비스)에서 gRPC Generated Source Directory 인식을 위해 추가 - 없어도 인식하려나?
sourceSets{
    main {
        proto {
            srcDirs("src/main/proto")
        }
        java {
            srcDirs("build/generated/source/main/java")
            srcDirs("build/generated/source/main/grpc")
        }
    }
}