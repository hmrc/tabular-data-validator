import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val appName: String = "tabular-data-validator"

ThisBuild / scalaVersion := "2.13.18"
ThisBuild / majorVersion := 1

lazy val project = Project(appName, file("."))

libraryDependencies ++= AppDependencies()

scalaSettings
defaultSettings()

Test / testOptions += Tests.Argument("-oD", "-u", "target/test-reports", "-h", "target/test-reports/html-report")

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
