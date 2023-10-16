rootProject.name = "data-fabric-platform"

includeBuild("build-logic")

includeBuild("aggregation")

includeBuild("libs")
includeBuild("utilities")
includeBuild("services")

//System.setProperty( "user.dir", project.projectDir.toString() )
System.setProperty("datafabric.platform.root", rootDir.absolutePath)