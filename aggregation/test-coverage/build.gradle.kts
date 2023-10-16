plugins {
    `kotlin-dsl`
    id("com.mobigen.report-aggregation")
    id("org.sonarqube") version "3.0"
}

val projectList = mutableListOf(
    "com.mobigen.libs:list",
    "com.mobigen.utilities:list",
    "com.mobigen.datafabric:sample"
)

val pathSonarSources  = mutableListOf(
    "libs/list/src/main/java",
    "utilities/list/src/main/java",
    "services/sample/src/main/java"
)

val pathSonarTests = mutableListOf(
    "libs/list/src/test/java",
    "utilities/list/src/test/java",
    "services/sample/src/test/java"
)

val pathSonarJavaBinaries = mutableListOf(
    "libs/list/build/classes/java/main",
    "utilities/list/build/classes/java/main",
    "services/sample/build/classes/java/main"
)

val pathSonarJavaTestBinaries = mutableListOf(
    "libs/list/build/classes/java/test",
    "utilities/list/build/classes/java/test",
    "services/sample/build/classes/java/test"
)

/* jblim : 최종 모습은 group:name으로 작성 시 sonar.sources, sonar.tests를 지금과 같이 추가해주는 것이다. 하지만 방법을 찾지 못함. */

dependencies {
    projectList.forEach { entry ->
        aggregate(entry)
    }
}

sonarqube {
    properties {
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "build/reports/jacoco/codeCoverageReport/codeCoverageReport.xml"
        )
        property("sonar.jacoco.reportPaths", "build/reports")

        property("sonar.projectKey", "Data-Fabric-Platform")
        property("sonar.projectName", "Data Fabric Platform")
        property("sonar.projectDescription", "For Data Fabric Platform")

        property("sonar.host.url", "https://sonarqube.iris.tools")
        property("sonar.login", "c211d0bfdf9496726e86b5ebc9097672b28cdae3")

        property("sonar.java.coveragePlugin", "jacoco")

        // get pwd absolute path
        property("sonar.projectBaseDir", System.getProperty("datafabric.platform.root"))

        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.java.source", "11")
        property("sonar.java.target", "11")

        property("sonar.sources", pathSonarSources)
        property("sonar.tests", pathSonarTests)
        property("sonar.java.binaries", pathSonarJavaBinaries)
//        property("sonar.java.test.binaries", pathSonarJavaTestBinaries)
//        property("sonar.tests", pathSonarTests)
//                property("sonar.java.binaries", pathSonarJavaBinaries)
//                property("sonar.java.test.binaries", pathSonarJavaTestBinaries)
//        property("sonar.sources", mutableListOf<String>(
//            projectList.get({ entry -> entry.value + "/src/main/java" })
//                "libs/list/src/main/java",
//                "utilities/list/src/main/java")
//        )
//        property("sonar.tests", mutableListOf<String>(
//            "libs/list/src/test/java")
//        )
//        property("sonar.java.binaries", mutableListOf<String>(
//            "libs/list/build/classes/java/main",
//            "utilities/list/build/classes/java/main")
//        )
//        property("sonar.java.test.binaries", mutableListOf<String>(
//            "libs/list/build/classes/java/test")
//        )
    }
}