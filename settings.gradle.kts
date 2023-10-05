rootProject.name = "data-fabric-platform"

includeBuild("build-logic")

includeBuild("libs")
includeBuild("utilities")
includeBuild("services")

//dependencyResolutionManagement {
//    versionCatalogs {
//        create("libs") {
//            from(files("./gradle/libs.versions.toml"))
//            version("lombokVer", "1.18.30")
//            library("lombok", "org.projectlombok", "lombok").versionRef("lombokVer")
//
//            version("log4jVer", "2.20.0")
//            library("log4j-api", "org.apache.logging.log4j", "log4j-api").versionRef("log4jVer")
//            library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4jVer")
//            library("log4j-slf4j-impl", "org.apache.logging.log4j", "log4j-slf4j-impl").versionRef("log4jVer")
//
//            version("jacksonYamlVer", "2.15.2")
//            library("jackson-dataformat-yaml", "javax.annotation", "javax.annotation-api").versionRef("javaxAnnotationVer")
//
//            plugin("protobuf-plugin", "com.google.protobuf").version("0.9.4")
//
//            version("protocVer", "3.24.3")
//            library("protoc", "com.google.protobuf", "protoc").versionRef("protocVer")
//            library("protoc-gen-grpc-java", "com.google.protobuf", "protobuf-java-util").versionRef( "protocVer" )
//
//            version("grpcVer", "1.56.1")
//           library("grpc-api", "io.grpc","grpc-api").versionRef("grpcVer")
//           library("grpc-stub", "io.grpc","grpc-stub").versionRef("grpcVer")
//           library("grpc-protobuf", "io.grpc","grpc-protobuf").versionRef("grpcVer")
//           library("grpc-services", "io.grpc","grpc-services").versionRef("grpcVer")
//           library("grpc-netty", "io.grpc","grpc-netty").versionRef("grpcVer")
//           library("grpc-testing", "io.grpc","grpc-testing").versionRef("grpcVer")
//
//            version("javaxAnnotationVer", "1.3.2")
//            library("javaxAnnotation", "javax.annotation", "javax.annotation-api").versionRef("javaxAnnotationVer")
//            version("junitVer", "5.9.3")
//            version("junitPlatformVer", "1.9.3")
//            library("junit", "org.junit.jupiter", "junit-jupiter").versionRef("junitVer")
//            library("junit-platform-launcher", "org.junit.platform", "junit-platform-launcher").versionRef("junitPlatformVer")
//            bundle("log4j", listOf("log4j-api", "log4j-core", "log4j-slf4j-impl"))
//            bundle("grpc", listOf("grpc-api", "grpc-stub", "grpc-protobuf", "grpc-services", "grpc-netty", "grpc-testing"))
//            protoc = [ "protoc", "protoc-gen-grpc-java" ]
//        }
//
//    }
//}