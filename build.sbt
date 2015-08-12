import SbtKitPre._

lazy val bombgrid = project in file(".") settings settings

lazy val settings = Settings(
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

watchSources ++= (baseDirectory.value * "*.sbt").get
watchSources ++= (baseDirectory.value / "project" * "*.scala").get
