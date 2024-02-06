plugins {
    `kotlin-dsl`
    id("com.mobigen.report-aggregation")
    id("org.sonarqube") version "4.4.1.3373"
}

val projectList = mapOf(
    "com.mobigen.datafabric.libs:rabbitmq" to "libs/rabbitmq",
    "com.mobigen.datafabric.services:core" to "services/core",
)

val srcPath = "src/main/java"
val testPath = "src/test/java"
val classPath = "build/classes/java/main"
val testClassPath = "build/classes/java/test"

/* jblim : 최종 모습은 group:name으로 작성 시 sonar.sources, sonar.tests를 지금과 같이 추가해주는 것이다. 하지만 방법을 찾지 못함. */
dependencies {
    projectList.forEach { entry ->
        aggregate(entry.key)
    }
}

sonar {
    properties {
        property("sonar.java.coveragePlugin", "jacoco")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "aggregation/test-coverage/build/reports/jacoco/codeCoverageReport/codeCoverageReport.xml"
        )
//        property("sonar.jacoco.reportPaths", "aggregation/test-coverage/build/reports")

        property("sonar.host.url", "https://sonarqube.iris.tools")
        property("sonar.login", "c211d0bfdf9496726e86b5ebc9097672b28cdae3")

        property("sonar.projectKey", "Data-Fabric-Platform")
        property("sonar.projectName", "Data Fabric Platform")
        property("sonar.projectDescription", "For Data Fabric Platform")

        // get pwd absolute path
        property("sonar.projectBaseDir", System.getProperty("datafabric.platform.root"))

        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.java.source", "17")
        property("sonar.java.target", "17")

        val pathSonarSources = mutableListOf<String>()
        val pathSonarTests = mutableListOf<String>()
        val pathSonarBinaries = mutableListOf<String>()
        val pathSonarTestBinaries = mutableListOf<String>()
        projectList.forEach { entry ->
            pathSonarSources.add("${entry.value}/$srcPath")
            pathSonarBinaries.add("${entry.value}/$classPath")
            if( File("${entry.value}/$testPath").isDirectory && File("${entry.value}/$testPath").listFiles() != null )
                pathSonarTests.add("${entry.value}/$testPath")
            if( File("${entry.value}/$testClassPath").isDirectory && File("${entry.value}/$testClassPath").listFiles() != null )
                pathSonarTestBinaries.add("${entry.value}/$testClassPath")
        }
        property("sonar.sources", pathSonarSources)
        property("sonar.tests", pathSonarTests)
        property("sonar.java.binaries", pathSonarBinaries)
        property("sonar.java.test.binaries", pathSonarTestBinaries)
    }
}