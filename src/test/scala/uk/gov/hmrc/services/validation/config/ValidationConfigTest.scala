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

package uk.gov.hmrc.services.validation.config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.services.validation.models.{CellDefinition, GroupRule, GroupRuleFlags, Rule}

class ValidationConfigTest extends WordSpec with Matchers {

  val configString: String =
    """
      | {
      |   fieldInfo: [
      |     {
      |      column = "A"
      |      cellName = "1. Date of event (yyyy-mm-dd)"
      |      mandatory = true
      |      error:
      |         {
      |          id = "error.1"
      |          errorMsg = "This is an error message"
      |          errorId = "001"
      |          isDate = true
      |         }
      |    }
      |    {
      |      column: "B"
      |      cellName = "Optional comment"
      |      mandatory: false
      |      error:
      |       {
      |        id = "error.2"
      |        errorMsg = "This is a second error message"
      |        errorId = "002"
      |        isDate = false
      |        regex = "[a-zA-Z ]*"
      |       }
      |     }
      |     {
      |       column: "C"
      |       cellName = "Optional something, 1.1111"
      |       mandatory: true
      |       error:
      |       {
      |        id = "error.3"
      |        errorMsg = "This is an error message for column C"
      |        errorId = "003"
      |        isDate = false
      |        regex = "[0-9]{1,13}\\.[0-9]{4}"
      |       }
      |     }
      |     {
      |       column: "D"
      |       cellName = "Optional D, mandatory if C has yes, int"
      |       mandatory: false
      |       error: {
      |          id = "error4"
      |          errorMsg = "This is an error message for column D"
      |          regex="[0-9]{1,6}"
      |          errorId = "002"
      |        }
      |     }
      |   ]
      |
      |   group-rules:[
      |    {
      |      id="mandatoryCD"
      |      errorId="999"
      |      flags: {
      |       independent="C"
      |       dependent="D"
      |      }
      |      expectedValue="1.1111"
      |      columnErrors: {
      |        "D": {errorMsg = "Field must have an entry."}
      |      }
      |    }
      |  ]
      |
      | }
    """.stripMargin
  val configParsed: Config = ConfigFactory.parseString(configString)

  val cellA = CellDefinition("A", "1. Date of event (yyyy-mm-dd)",
    mandatory = true,
    Rule("error.1", "001", "This is an error message", isDate = true, None))
  val cellB = CellDefinition("B", "Optional comment",
    mandatory = false,
    Rule("error.2", "002", "This is a second error message", isDate = false, Some("[a-zA-Z ]*")))
  val cellC = CellDefinition("C", "Optional something, 1.1111",
    mandatory = true,
    Rule("error.3", "003", "This is an error message for column C", isDate = false, Some("[0-9]{1,13}\\.[0-9]{4}")))
  val cellD = CellDefinition("D", "Optional D, mandatory if C has yes, int",
    mandatory = false,
    Rule("error4", "002", "This is an error message for column D", isDate = false, Some("[0-9]{1,6}")))

  "ValidationConfig" should {
    "return a valid list of cellDefinitions" in new ValidationConfig(configParsed) {
      cells shouldBe List(
        cellA,
        cellB,
        cellC,
        cellD
      )
    }
    "return a valid list of cells by column" in new ValidationConfig(configParsed) {
      cellsByColumn shouldBe Map("A" -> cellA, "B" -> cellB, "C" -> cellC, "D" -> cellD)
    }
    "return a valid list of groupRules" in new ValidationConfig(configParsed) {
      groupRules shouldBe Some(List(
        GroupRule("mandatoryCD", "999", Map("D" -> "Field must have an entry."),
          GroupRuleFlags("C", "D"), "1.1111")
      ))
    }
  }
}
