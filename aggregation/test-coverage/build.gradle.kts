plugins {
    id("com.mobigen.report-aggregation")
    id("org.sonarqube") version "6.0.1.5171"
}

dependencies {
    // Transitively collect coverage data from all features and their dependencies
//    aggregate("com.mobigen.vdap.share:models")
//    aggregate("com.example.myproduct.admin-feature:config")
}

tasks

