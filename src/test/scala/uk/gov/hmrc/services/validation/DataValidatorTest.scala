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

  class RowTestDataValidator(config: Config) extends DataValidator(config) {
    override def validateCell(cell: Cell): Option[ValidationError] = None
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
        val invalidEntries = List("n0t valid", " extra-inValid", "probably still 1nvalid")
        invalidEntries.foreach { entry =>
          val cell = Cell("B", 123, entry)
          val resOpt: Option[ValidationError] = validator.validateCell(cell)
          resOpt.get shouldBe ValidationError(cell, "error.2", "002", "This is a second error message")
        }
      }

      "return a None if the cell is filled in correctly" in {
        val validEntries = List("valid", " extraValid", "probably still valid")
        validEntries.foreach { entry =>
          val cell = Cell("B", 123, entry)
          val resOpt: Option[ValidationError] = validator.validateCell(cell)
          resOpt shouldBe None
        }
      }
    }
  }

  class HelperDataRows(config: Config = CONFIG) {
    val validator: DataValidator = new RowTestDataValidator(config)
    val happyCNeedD: Cell = Cell("C", 1, "1.1111")
    val happyCNoD: Cell = Cell("C", 1, "2.2222")
    val happyD: Cell = Cell("D", 1, "11")
    val emptyC: Cell = Cell("C", 1, "")
    val emptyD: Cell = Cell("D", 1, "")
  }

  "Row validation" must {
    "validate group rule" when {
      "the independent cell doesn't have the expected value and the dependent cell is empty" in new HelperDataRows() {
        val row: Row = Row(1, Seq(happyCNoD, emptyD))
        val validatedRow: Option[List[ValidationError]] = validator.validateRow(row)
        assert(validatedRow.isEmpty)
      }
      "the independent call has the expected value and the dependent cell is present" in new HelperDataRows {
        val row: Row = Row(1, Seq(happyCNeedD, happyD))
        val validatedRow: Option[List[ValidationError]] = validator.validateRow(row)
        assert(validatedRow.isEmpty)
      }
    }
    "not validate failing group rule" when {
      "then independent cell has the expected value but the dependent value is empty" in new HelperDataRows {
        val row: Row = Row(1, Seq(happyCNeedD))
        val validatedRow: Option[List[ValidationError]] = validator.validateRow(row)
        assert(validatedRow.isDefined)
        validatedRow.get.length shouldBe 1
        validatedRow.get.head shouldBe ValidationError(emptyD, "mandatoryCD", "999", "Field must have an entry.")
      }

    }
    "validate when there are no group rules" in {
      val emptyConfig =
        """{
          |fieldInfo: []
          |}
          |""".stripMargin

      val validator: DataValidator = new RowTestDataValidator(ConfigFactory.parseString(emptyConfig))
      val validatedRow: Option[List[ValidationError]] = validator.validateRow(Row(1, Seq(Cell("A", 1, ""))))
      assert(validatedRow.isEmpty)
    }
    "throw exception when rule refers to non-existent cell" in {
      val invalidConfig =
        """{
          |   fieldInfo: []
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
          |}
          |""".stripMargin
      val validator: DataValidator = new RowTestDataValidator(ConfigFactory.parseString(invalidConfig))
      intercept[RuntimeException](validator.validateRow(Row(1, Seq(Cell("C", 1, "1.1111")))))
        .getMessage shouldBe "[DataValidator][validateRow] The columns defined in fieldInfo did not have a definition for one of the columns in group-rules."
    }

    "validate when working together with validateCell" in {
      val validator: DataValidator = new DataValidator(CONFIG)
      val row = Row(1, Seq(Cell("B", 1, "1234"), Cell("C", 1, "1.1111")))
      val validatedRow = validator.validateRow(row)
      assert(validatedRow.isDefined)
      validatedRow.get.length shouldBe 3
      validatedRow.get shouldBe List(
        ValidationError(Cell("D", 1, ""), "mandatoryCD", "999", "Field must have an entry."),
        ValidationError(Cell("A", 1, ""), "error.1", "001", "This is an error message"),
        ValidationError(Cell("B", 1, "1234"), "error.2", "002", "This is a second error message")
      )
    }
  }

  "pass valid data in optional field" in {
    val validator = new DataValidator(CONFIG)
    val valids = List("this", "sampleText", "blablabla", "somethingsomething")
    valids.foreach { validValue =>
      val cell = Cell("B", 123, validValue)
      val resOpt: Option[ValidationError] = validator.validateCell(cell)
      resOpt shouldBe None
    }
  }

  "pass empty data in optional field" in {
    val validator = new DataValidator(CONFIG)
    EMPTIES.foreach { emptyValue =>
      val cell = Cell("B", 123, emptyValue)
      val resOpt: Option[ValidationError] = validator.validateCell(cell)
      resOpt shouldBe None
    }
  }
}