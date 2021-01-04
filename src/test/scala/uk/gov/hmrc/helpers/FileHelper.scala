/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.helpers

import java.io.{File, FileReader}

import au.com.bytecode.opencsv.CSVReader
import com.typesafe.config.{Config, ConfigFactory}
import scala.collection.JavaConverters._

object FileHelper {

  def readInputCsv(file: File): List[List[String]] = {
    val reader = new CSVReader(new FileReader(file))

    def readLines(reader: CSVReader): List[List[String]] = {
      reader.readAll.asScala.toList.map(_.toList)
    }
    readLines(reader)
  }

  def loadConfig(file: File): Config = {
    ConfigFactory.parseFile(file)
  }
}
