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

package uk.gov.hmrc.services.validation

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.OptionValues._
import uk.gov.hmrc.services.validation.models._

class DataValidatorTest extends WordSpec with Matchers {

  val CONFIG_STR: String =
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
      |          errorMsg = "Not like this my dude"
      |          regex="[0-9]{1,6}"
      |          errorId = "002"
      |        }
      |     }
      |   ]
      |
      |   group-rules:[
      |    {
      |      id="mandatoryCD"
      |      errorId="102"
      |      columns:["C", "D"]
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
  val CONFIG: Config = ConfigFactory.parseString(CONFIG_STR)

  val EMPTIES: List[String] = List("", " ", "  ", "                ")

  def expectErrors(row: Row, expectedErrors: List[ValidationError], config: Config = CONFIG) = {
    val validator = new DataValidator(config)
    val maybeErrors: Option[List[ValidationError]] = validator.validateRow(row)
    maybeErrors should not be None
    maybeErrors.value shouldBe expectedErrors
  }

  def expectOk(row: Row, config: Config = CONFIG) = {
    val validator = new DataValidator(config)
    val maybeErrors: Option[List[ValidationError]] = validator.validateRow(row)
    maybeErrors should be(None)
  }

  "Cell validation" when {
    val validator = new DataValidator(CONFIG)

    "evaluating a mandatory cell" should {
      "return a validation error if the cell is empty" in {
        EMPTIES.foreach { emptyValue =>
          val cell = Cell("A", 123, emptyValue)
          val resOpt: Option[ValidationError] = validator.validateCell(cell)
          resOpt should not be None
          resOpt.get shouldBe ValidationError(cell, "error.1", "001", "This is an error message")
        }
      }

      "return a validation error if expecting a date and not receiving one" in {
        val cell = Cell("A", 123, "notADateFam")
        val resOpt: Option[ValidationError] = validator.validateCell(cell)
        resOpt should not be None
        resOpt.get shouldBe ValidationError(cell, "error.1", "001", "This is an error message")
      }

      "return None if the cell is valid" in {
        val cell = Cell("A", 123, "1990-11-10")
        val resOpt: Option[ValidationError] = validator.validateCell(cell)
        resOpt shouldBe None
      }

    }

    "evaluating an optional cell" should {
      "return a None if the cell is empty" in {
        EMPTIES.foreach { emptyValue =>
          val cell = Cell("B", 123, emptyValue)
          val resOpt: Option[ValidationError] = validator.validateCell(cell)
          resOpt shouldBe None
        }
      }

      "return a validation error if cell is filled incorrectly" in {
        val invalidEntries =  List("n0t valid", " extra-inValid", "probably still 1nvalid")
        invalidEntries.foreach { entry =>
          val cell = Cell("B", 123, entry)
          val resOpt: Option[ValidationError] = validator.validateCell(cell)
          resOpt.get shouldBe ValidationError(cell, "error.2", "002", "This is a second error message")
        }
      }

      "return a None if the cell is filled in correctly" in {
        val validEntries =  List("valid", " extraValid", "probably still valid")
        validEntries.foreach { entry =>
          val cell = Cell("B", 123, entry)
          val resOpt: Option[ValidationError] = validator.validateCell(cell)
          resOpt shouldBe None
        }
      }
    }
  }

/*
  "Row validation" should {
    val rowNum = 123
    val VALID_B = "Polite comment."
    val VALID_C = "anything is valid"
    val VALID_D = "123456"

    val INVALID_B = "damn comment."
    val INVALID_D = "short"

    val B_VALID = Cell("B", rowNum, VALID_B)
    val C_VALID = Cell("C", rowNum, VALID_C)
    val D_VALID = Cell("D", rowNum, VALID_D)

    val B_WRONG = Cell("B", rowNum, INVALID_B)
    val D_WRONG = Cell("D", rowNum, INVALID_D)

    val B_EMPTY = Cell("B", rowNum, EMPTIES(0))
    val C_EMPTY = Cell("C", rowNum, EMPTIES(1))
    val D_EMPTY = Cell("D", rowNum, EMPTIES(2))

    val ROW_EMPTIES = Row(rowNum, Seq(B_EMPTY, C_EMPTY, D_EMPTY))

    "raise errors for appropriate cells on wrong row values" in {
      val row = ROW_EMPTIES
      expectErrors(row, List(
        ValidationError(B_EMPTY, "mandatoryBCD", "102", "'Optional comment' or 'Optional something' or 'Optional D' must have an entry."),
        ValidationError(C_EMPTY, "mandatoryBCD", "102", "Field must have an entry.")
      ))
    }

    "pass appropriate cells with proper row values" in {
      val rows = Seq(
        Row(rowNum, Seq(B_EMPTY,  C_EMPTY,  D_VALID)),
        Row(rowNum, Seq(B_EMPTY,  C_VALID,  D_EMPTY)),
        Row(rowNum, Seq(B_VALID,  C_EMPTY,  D_EMPTY)),
        Row(rowNum, Seq(B_VALID,  C_EMPTY,  D_VALID)),
        Row(rowNum, Seq(B_VALID,  C_VALID,  D_EMPTY)),
        Row(rowNum, Seq(B_VALID,  C_VALID,  D_VALID))
      )

      rows.foreach { expectOk(_) }
    }

    "invoke individual cells validation with proper row values" in {
      val ERROR_B = ValidationError(B_WRONG, "custom.2", "errorId_2", "Comment must be polite!")
      expectErrors(Row(rowNum, Seq(B_WRONG, C_EMPTY, D_EMPTY)), List(ERROR_B))
      expectErrors(Row(rowNum, Seq(B_WRONG, C_VALID, D_EMPTY)), List(ERROR_B))
      expectErrors(Row(rowNum, Seq(B_WRONG, C_EMPTY, D_VALID)), List(ERROR_B))
      expectErrors(Row(rowNum, Seq(B_WRONG, C_VALID, D_VALID)), List(ERROR_B))

      val ERROR_D = ValidationError(D_WRONG, "custom.D", "errorId_D", "'Optional D' is too short!")
      expectErrors(Row(rowNum, Seq(B_EMPTY, C_EMPTY, D_WRONG)), List(ERROR_D))
      expectErrors(Row(rowNum, Seq(B_VALID, C_EMPTY, D_WRONG)), List(ERROR_D))
      expectErrors(Row(rowNum, Seq(B_EMPTY, C_VALID, D_WRONG)), List(ERROR_D))
      expectErrors(Row(rowNum, Seq(B_VALID, C_VALID, D_WRONG)), List(ERROR_D))

      expectErrors(Row(rowNum, Seq(B_WRONG, C_EMPTY, D_WRONG)), List(ERROR_B, ERROR_D))
      expectErrors(Row(rowNum, Seq(B_WRONG, C_VALID, D_WRONG)), List(ERROR_B, ERROR_D))
    }
  }

 */

}
