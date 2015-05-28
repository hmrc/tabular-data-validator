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

import sbt.Keys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object HmrcBuild extends Build {
  import uk.gov.hmrc.DefaultBuildSettings
  import uk.gov.hmrc.DefaultBuildSettings._

  val nameApp = "tabular-data-validator"

  val appDependencies = Seq(
    "org.mvel" % "mvel2" % "2.2.0.Final",
    "com.typesafe" % "config" % "1.2.0",
    "org.scalatest" %% "scalatest" % "2.2.0" % "test",
    "org.pegdown" % "pegdown" % "1.4.2" % "test",
    "net.sf.opencsv" % "opencsv" % "2.3" % "test"
  )

  lazy val project = Project(nameApp, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      targetJvm := "jvm-1.7",
      libraryDependencies ++= appDependencies,
      organization := "uk.gov.hmrc",
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/"
      ),
      testOptions in Test += Tests.Argument("-oD","-u", "target/test-reports", "-h", "target/test-reports/html-report")
    )
}