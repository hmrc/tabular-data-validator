/*
 *
 * Copyright 2015 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import de.heikoseeberger.sbtheader.AutomateHeaderPlugin
import sbt.Keys._
import sbt._

object HmrcBuild extends Build {
  import uk.gov.hmrc.DefaultBuildSettings
  import DefaultBuildSettings._
  import uk.gov.hmrc.{SbtBuildInfo, ShellPrompt}
  import de.heikoseeberger.sbtheader.HeaderPlugin

  val nameApp = "data-validation"
  val versionApp = "1.0.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.mvel" % "mvel2" % "2.2.0.Final",
    "com.typesafe" % "config" % "1.2.0",
    "org.scalatest" %% "scalatest" % "2.2.0" % "test",
    "org.pegdown" % "pegdown" % "1.4.2" % "test",
    "net.sf.opencsv" % "opencsv" % "2.3" % "test"
  )

  lazy val playBreadcrumb = Project(nameApp, file("."))
    .settings(version := versionApp)
    .settings(scalaSettings : _*)
    .settings(defaultSettings(false) : _*)
    .settings(
      targetJvm := "jvm-1.7",
      shellPrompt := ShellPrompt(versionApp),
      libraryDependencies ++= appDependencies,
      organization := "uk.gov.hmrc",
      resolvers := Seq(
        Opts.resolver.sonatypeReleases,
        Opts.resolver.sonatypeSnapshots,
        "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/",
        "typesafe-snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
        "typesafe-mwn" at "http://repo.typesafe.com/typesafe/maven-releases/"
      ),
      testOptions in Test += Tests.Argument("-oD","-u", "target/test-reports", "-h", "target/test-reports/html-report")
    )
    .settings(SbtBuildInfo(): _*)
    .enablePlugins(AutomateHeaderPlugin)
    .settings(HeaderSettings())


}



object HeaderSettings {

  import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
  import de.heikoseeberger.sbtheader.license.Apache2_0

  def apply() = headers := Map("scala" -> Apache2_0("2015", "HM Revenue & Customs"))
}
