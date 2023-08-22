import sbt.*
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val appName: String = "tabular-data-validator"

lazy val project = Project(appName, file("."))

enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)

libraryDependencies ++= AppDependencies()
libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)
majorVersion := 1

scalaSettings
defaultSettings()
scalaVersion := "2.13.11"

Test / testOptions += Tests.Argument("-oD","-u", "target/test-reports", "-h", "target/test-reports/html-report")

addCommandAlias("scalastyleAll", "all scalastyle")
