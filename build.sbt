import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "jsasync",
    libraryDependencies += munit % Test,
    assembly / assemblyOutputPath := new File(target.value + "/jsasync.jar"),
    assembly / assemblyMergeStrategy := {
        case PathList("module-info.class") => MergeStrategy.last
        case path if path.endsWith("/module-info.class") => MergeStrategy.last
        case x =>
            val oldStrategy = (assembly / assemblyMergeStrategy).value
            oldStrategy(x)
    }
  )

