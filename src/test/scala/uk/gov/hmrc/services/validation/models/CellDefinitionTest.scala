/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.services.validation.models

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}

class CellDefinitionTest extends WordSpec with Matchers {
  val configString: String =
    """
      | {
      |  column = "A"
      |  cellName = "1. Date of event (yyyy-mm-dd)"
      |  mandatory = true
      |  error:
      |     {
      |      id = "error.1"
      |      errorMsg = "This is an error message"
      |      errorId = "001"
      |      isDate = true
      |     }
      |}
      |""".stripMargin

  val configParsed: Config = ConfigFactory.parseString(configString)

  "CellDefinition" should {
    "parse config into a CellDefinition object" in {
      CellDefinition(configParsed) shouldBe CellDefinition(
        "A",
        "1. Date of event (yyyy-mm-dd)",
        mandatory = true,
        Rule("error.1", "001", "This is an error message", isDate = true, None)
      )
    }
  }
}
