import SbtKitPre._

lazy val bombgrid = project in file(".") dependsOn (projectsCpDeps: _*) aggregate (projectsRefs: _*) settings(
  settings0,
  noArtifacts
)

lazy val projects = List(api, tui, aui, iui, gui, web)
lazy val projectsCpDeps = projects.map(x => x: ClasspathDep[ProjectReference])
lazy val projectsRefs   = projects.map(x => x: ProjectReference)

lazy val api = project settings settings1
lazy val tui = project settings settings1 dependsOn api
lazy val aui = project settings settings1 dependsOn api
lazy val iui = project settings settings1 dependsOn api
lazy val gui = project settings settings1 dependsOn api
lazy val web = project settings settings1 dependsOn api

lazy val settings0 = Settings(
  organization := "com.dwijnand",
       version := "0.1.0-SNAPSHOT",

        scalaVersion := "2.11.7",
  crossScalaVersions := Seq(scalaVersion.value),
          crossPaths := false,

  maxErrors := 5,
  triggeredMessage := Watched.clearWhenTriggered,

  scalacOptions ++= Seq("-encoding", "utf8"),
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
  scalacOptions  += "-language:higherKinds",
  scalacOptions  += "-language:implicitConversions",
  scalacOptions  += "-language:postfixOps",
  scalacOptions  += "-Xfuture",
  scalacOptions  += "-Yinline-warnings",
  scalacOptions  += "-Yno-adapted-args",
  scalacOptions  += "-Ywarn-dead-code",
  scalacOptions  += "-Ywarn-numeric-widen",
  scalacOptions  += "-Ywarn-unused-import",
  scalacOptions  += "-Ywarn-value-discard",

  scalacOptions in (Compile, console) -= "-Ywarn-unused-import",
  scalacOptions in (Test,    console) -= "-Ywarn-unused-import",

  wartremoverWarnings ++= Warts.unsafe,
  wartremoverWarnings  += Wart.FinalCaseClass,
  wartremoverWarnings  += Wart.JavaConversions,
  wartremoverWarnings  += Wart.MutableDataStructures,
  wartremoverWarnings  -= Wart.Throw, // Breaks on tuple destructuring

  initialCommands in console += "\nimport com.dwijnand.bombgrid._",

  parallelExecution in Test := true,
  fork in Test := false,

  fork in run := true,
  cancelable in Global := true,

  sources in (Compile, doc) := Nil,
  publishArtifact in (Compile, packageDoc) := false
)

val settings1 = Settings(
  settings0,
  name := s"bombgrid-${name.value}"
)

val noPackage = Settings(Keys.`package` := file(""), packageBin := file(""), packagedArtifacts := Map())
val noPublish = Settings(publish :=(), publishLocal :=(), publishArtifact := false)
val noArtifacts = Settings(noPackage, noPublish)

watchSources ++= (baseDirectory.value * "*.sbt").get
watchSources ++= (baseDirectory.value / "project" * "*.scala").get
