plugins {
    id("java")
    id("jacoco")
}

jacoco{
    toolVersion = "0.8.12"
}

// Do not generate reports for individual projects
tasks.jacocoTestReport.configure {
    enabled = false
//    reports {
//        xml.required = true
//        csv.required = false
//        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
//    }
}