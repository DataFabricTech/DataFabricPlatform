plugins {
    id("java")
    id("jacoco")
}

jacoco{
    toolVersion = "0.8.10"
}

// Do not generate reports for individual projects
tasks.jacocoTestReport.configure {
    enabled = false
}