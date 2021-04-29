import sbt._
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val appName: String = "tabular-data-validator"

val appDependencies = Seq(
    "com.typesafe" % "config" % "1.4.1",
    "org.scalatest" %% "scalatest" % "3.0.9" % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test"
)

lazy val project = Project(appName, file("."))

enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)

libraryDependencies ++= appDependencies
majorVersion := 1

scalaSettings
defaultSettings()
scalaVersion := "2.12.12"

testOptions in Test += Tests.Argument("-oD","-u", "target/test-reports", "-h", "target/test-reports/html-report")
