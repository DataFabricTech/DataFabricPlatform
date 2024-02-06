import com.google.protobuf.gradle.*

plugins {
    id("com.mobigen.java-library")
    id("com.google.protobuf") version "0.9.4"
}

group = "${group}.share"
version = "0.0.1"

dependencies {
    // protobuf
    implementation(Dependencies.Protobuf.PROTOBUF_UTIL)
    // gRPC
    implementation(Dependencies.GRPC.PROTOBUF)
    implementation(Dependencies.GRPC.STUB)

    if (JavaVersion.current().isJava9Compatible()) {
        compileOnly("javax.annotation:javax.annotation-api:1.3.2") // Java 9+ compatibility - Do NOT update to 2.0.0
//        annotationProcessor("jakarta.annotation:jakarta.annotation-api:3.0.0-M1")
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
    /*
     * ProtoBuf를 통해 생성된 파일의 위치를 지정하게되는 경우
     * proto 파일 변경이 올바르게 반영되지 않는 문제가 발생할 수 있다.
     * 따라서, 아래와 같이 설정하지 않는다.
    **/
    // generatedFilesBaseDir = "$projectDir/src/generated"
    generateProtoTasks {
        // generate proto files
        ofSourceSet("main").forEach { task ->
            task.plugins {
                id("grpc") {
                }
            }
        }
    }
}

// build / resource 의 중복을 허용 : proto file 복사 시 에러 발생
tasks {
    processResources{
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

sourceSets{
    main {
        proto{
            srcDirs("src/main/proto")
        }
        java {
            srcDirs("build/generated/source/proto/main/java")
            srcDirs("build/generated/source/proto/main/grpc")
        }
    }
}

// Optional
idea {
    module {
        sourceDirs.add(File("src/main/proto"))
        generatedSourceDirs.add(File("build/generated/source/proto/main/java"))
        generatedSourceDirs.add(File("build/generated/source/proto/main/grpc"))
    }
}