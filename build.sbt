/*
 *
 * Copyright 2020 HM Revenue & Customs
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

import sbt._
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val appName: String = "tabular-data-validator"

val appDependencies = Seq(
    "com.typesafe" % "config" % "1.4.1",
    "org.scalatest" %% "scalatest" % "3.0.9" % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test"
)

lazy val project = Project(appName, file("."))
  .settings(
    testOptions in Test += Tests.Argument("-oD","-u", "target/test-reports", "-h", "target/test-reports/html-report")
  )

enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)

libraryDependencies ++= appDependencies
majorVersion := 1

scalaSettings
defaultSettings()
scalaVersion := "2.12.12"

resolvers := Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  "typesafe-releases" at "https://repo.typesafe.com/typesafe/releases/"
)
