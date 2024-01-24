import com.google.protobuf.gradle.*

plugins {
    id("com.mobigen.java-library")
    id("com.google.protobuf") version "0.9.4"
}

group = "com.mobigen.share"
version = "0.0.1"

dependencies {
    // protobuf
    implementation(Dependency.protobuf.protoBufUtil)
    // gRPC
    implementation(Dependency.grpc.stub)
    implementation(Dependency.grpc.protobuf)
    implementation(Dependency.grpc.service)
    implementation(Dependency.grpc.netty)
//    implementation(Dependency.grpc.testing)

    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.1")

    if (JavaVersion.current().isJava9Compatible()) {
        implementation("javax.annotation:javax.annotation-api:1.3.2")
    }
}

protobuf {
    protoc {
        artifact = Dependency.protobuf.protoc
    }
    plugins {
        id("grpc") {
            artifact = Dependency.protobuf.protocGenJava
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