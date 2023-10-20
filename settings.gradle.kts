rootProject.name = "data-fabric-platform"

includeBuild("build-logic")

includeBuild("aggregation")

includeBuild("share")
includeBuild("libs")
includeBuild("services")

//System.setProperty( "user.dir", project.projectDir.toString() )
System.setProperty("datafabric.platform.root", rootDir.absolutePath)
