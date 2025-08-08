import sbt.*
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val appName: String = "tabular-data-validator"

lazy val project = Project(appName, file("."))

libraryDependencies ++= AppDependencies()
majorVersion := 1

scalaSettings
defaultSettings()
scalaVersion := "2.13.16"

Test / testOptions += Tests.Argument("-oD","-u", "target/test-reports", "-h", "target/test-reports/html-report")
