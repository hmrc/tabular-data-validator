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
import uk.gov.hmrc.services.validation.config.RuleRef

class DataValidatorTest extends WordSpec with Matchers {

  val CONFIG_STR: String =
    """
      | {
      |   fieldInfo: [
      |     {
      |      column = "A"
      |      cellName = "1. Date of event (yyyy-mm-dd)"
      |      mandatory = true
      |      error: [
      |       {
      |        id = "error.1"
      |        errorMsg = "this is an error message"
      |        errorId = "001"
      |        isDate = true
      |       }
      |      ]
      |    }
      |     {
      |       column: "B"
      |       cellName = "Optional comment"
      |       mandatory: true
      |     }
      |     {
      |       column: "C"
      |       cellName = "Optional something"
      |       mandatory: true
      |     }
      |     {
      |       column: "D"
      |       cellName = "Optional D, mandatory if C has yes"
      |       mandatory: false
      |     }
      |   ]
      |
      |   group-rules:[
      |    {
      |      id="mandatoryBCD"
      |      errorId="102"
      |      columns:["C", "D"]
      |      flags: {
      |       independent="C"
      |       dependent="D"
      |      }
      |      expectedValue="yes"
      |      columnErrors: {
      |        "D": {errorMsg = "Field must have an entry."}
      |      }
      |    }
      |  ]
      |
      |   rules: [
      |     {
      |       id="MANDATORY"
      |       errorId="002"
      |       errorMsgTemplate = "@{cellName} must have an entry."
      |       regex="!(data == null || data.trim().isEmpty())"
      |       isDate: false
      |     }
      |   ]
      | }
    """.stripMargin
  val CONFIG: Config = ConfigFactory.parseString(CONFIG_STR)

  val EMPTIES: List[String] = List("", " ", "  ", "                ")

//  def expectErrors(row: Row, expectedErrors: List[ValidationError], config: Config = CONFIG) = {
//    val validator = DataValidator(config)
//    val maybeErrors: Option[List[ValidationError]] = validator.validateRow(row)
//    maybeErrors should not be empty
//    maybeErrors.value shouldBe expectedErrors
//  }
//
//  def expectErrorsInRows(rows: List[List[String]], expectedErrors: List[ValidationError], config: Config = CONFIG, contextObjectOpt: Option[AnyRef] = None, ignoreBlankRows: Boolean = false) = {
//    val maybeErrors: Option[List[ValidationError]] = DataValidator(config).validateRows(rows, contextObjectOpt, 0, ignoreBlankRows)
//    maybeErrors should not be empty
//
//    /*
//    for(i <- 0 until expectedErrors.size) {
//      maybeErrors.foreach(list => list.lift(i).foreach(println))
//      println(expectedErrors(i))
//      println
//      println
//    }
//*/
//
//    maybeErrors.value shouldBe expectedErrors
//  }

  def expectOk(row: Row, config: Config = CONFIG) = {
    val validator = DataValidator(config)
    val maybeErrors: Option[List[ValidationError]] = validator.validateRow(row)
    maybeErrors should be(None)
  }

//  def expectOkInRows(rows: List[List[String]], config: Config = CONFIG) = {
//    val maybeErrors: Option[List[ValidationError]] = DataValidator(config).validateRows(rows, None)
//    maybeErrors should be(None)
//  }

  "Mandatory validation" should {
    "fail for no input" in {
      val validator = DataValidator(CONFIG)
      val cell = Cell("A", 123, "")
      val resOpt: Option[List[ValidationError]] = validator.validateCell(cell)
      resOpt should not be None
//      resOpt.value shouldBe List()
    }

    "work with incorrect date filled" in {
      val validator = DataValidator(CONFIG)
      val cell = Cell("A", 123, "notADateFam")
      val resOpt: Option[List[ValidationError]] = validator.validateCell(cell)
      resOpt should not be None
//      resOpt.value shouldBe List()
    }

    "work with correct date filled" in {
      val validator = DataValidator(CONFIG)
      val cell = Cell("A", 123, "11-10-1990")
      val resOpt: Option[List[ValidationError]] = validator.validateCell(cell)
      resOpt shouldBe None
      //      resOpt.value shouldBe List()
    }
  }


//    "Mandatory validation" should {
//        "give mandatory error on mandatory field empty" in {
//          val validator = DataValidator(CONFIG)
//          EMPTIES.foreach { case emptyValue =>
//            val cell = Cell("A", 123, emptyValue)
//            val resOpt: Option[List[ValidationError]] = validator.validateCell(cell)
//              resOpt should not be empty
//              resOpt.value shouldBe List(
//                ValidationError(cell, RuleRef.MANDATORY_RULE_REF.id, "002", "Greeting must have an entry.")
//              )
//          }
//        }
//
//      "give errors on mandatory field invalid values" in {
//        val validator = DataValidator(CONFIG)
//        val invalids: List[String] = List("bla", " Hello, World!", "hello world", "blablabla")
//        invalids.foreach { case invalidValue =>
//          val cell = Cell("A", 123, invalidValue)
//          val resOpt: Option[List[ValidationError]] = validator.validateCell(cell)
//          resOpt should not be empty
//          resOpt.value shouldBe List(
//            ValidationError(cell, "custom", "errorId_1", "Greeting is not a helloworld.")
//          )
//        }
//      }
//
//      "pass mandatory field with proper value" in {
//        val validator = DataValidator(CONFIG)
//        val cell = Cell("A", 123, "Hello, World!")
//        val resOpt: Option[List[ValidationError]] = validator.validateCell(cell)
//        resOpt should be (None)
//      }
//
//
//    }
//
//    "Optional validation" should {
//      "give errors on non-empty invalid data in optional field" in {
//        val validator = DataValidator(CONFIG)
//        val invalids: List[String] = List("damn things", "So damn!", "This was damn situation!")
//        invalids.foreach { case invalidValue =>
//          val cell = Cell("B", 123, invalidValue)
//          val resOpt: Option[List[ValidationError]] = validator.validateCell(cell)
//          resOpt should not be empty
//          resOpt.value shouldBe List(
//            ValidationError(cell, "custom.2", "errorId_2", "Comment must be polite!")
//          )
//        }
//      }
//
//      "pass valid data in optional field" in {
//        val validator = DataValidator(CONFIG)
//        val valids = List("This is my comment.", "It was great", "blablabla", "something-something")
//        valids.foreach { case validValue =>
//          val cell = Cell("B", 123, validValue)
//          val resOpt: Option[List[ValidationError]] = validator.validateCell(cell)
//          resOpt shouldBe None
//        }
//      }
//
//      "pass empty data in optional field" in {
//        val validator = DataValidator(CONFIG)
//        EMPTIES.foreach { case emptyValue =>
//          val cell = Cell("B", 123, emptyValue)
//          val resOpt: Option[List[ValidationError]] = validator.validateCell(cell)
//          resOpt shouldBe None
//        }
//      }
//    }
//
//  "Row validation" should {
//    val rowNum = 123
//    val VALID_B = "Polite comment."
//    val VALID_C = "anything is valid"
//    val VALID_D = "123456"
//
//    val INVALID_B = "damn comment."
//    val INVALID_D = "short"
//
//    val B_VALID = Cell("B", rowNum, VALID_B)
//    val C_VALID = Cell("C", rowNum, VALID_C)
//    val D_VALID = Cell("D", rowNum, VALID_D)
//
//    val B_WRONG = Cell("B", rowNum, INVALID_B)
//    val D_WRONG = Cell("D", rowNum, INVALID_D)
//
//    val B_EMPTY = Cell("B", rowNum, EMPTIES(0))
//    val C_EMPTY = Cell("C", rowNum, EMPTIES(1))
//    val D_EMPTY = Cell("D", rowNum, EMPTIES(2))
//
//    val ROW_EMPTIES = Row(rowNum, Seq(B_EMPTY, C_EMPTY,D_EMPTY))
//
//    "raise errors for appropriate cells on wrong row values" in {
//      val row = ROW_EMPTIES
//      expectErrors(row, List(
//        ValidationError(B_EMPTY, "mandatoryBCD", "102", "'Optional comment' or 'Optional something' or 'Optional D' must have an entry."),
//        ValidationError(C_EMPTY, "mandatoryBCD", "102", "Field must have an entry.")
//      ))
//    }
//
//    "pass appropriate cells with proper row values" in {
//      val rows = Seq(
//        Row(rowNum, Seq(B_EMPTY,  C_EMPTY,  D_VALID)),
//        Row(rowNum, Seq(B_EMPTY,  C_VALID,  D_EMPTY)),
//        Row(rowNum, Seq(B_VALID,  C_EMPTY,  D_EMPTY)),
//        Row(rowNum, Seq(B_VALID,  C_EMPTY,  D_VALID)),
//        Row(rowNum, Seq(B_VALID,  C_VALID,  D_EMPTY)),
//        Row(rowNum, Seq(B_VALID,  C_VALID,  D_VALID))
//      )
//
//      rows.foreach { expectOk(_) }
//    }
//
//    "invoke individual cells validation with proper row values" in {
//      val ERROR_B = ValidationError(B_WRONG, "custom.2", "errorId_2", "Comment must be polite!")
//      expectErrors(Row(rowNum, Seq(B_WRONG, C_EMPTY, D_EMPTY)), List(ERROR_B))
//      expectErrors(Row(rowNum, Seq(B_WRONG, C_VALID, D_EMPTY)), List(ERROR_B))
//      expectErrors(Row(rowNum, Seq(B_WRONG, C_EMPTY, D_VALID)), List(ERROR_B))
//      expectErrors(Row(rowNum, Seq(B_WRONG, C_VALID, D_VALID)), List(ERROR_B))
//
//      val ERROR_D = ValidationError(D_WRONG, "custom.D", "errorId_D", "'Optional D' is too short!")
//      expectErrors(Row(rowNum, Seq(B_EMPTY, C_EMPTY, D_WRONG)), List(ERROR_D))
//      expectErrors(Row(rowNum, Seq(B_VALID, C_EMPTY, D_WRONG)), List(ERROR_D))
//      expectErrors(Row(rowNum, Seq(B_EMPTY, C_VALID, D_WRONG)), List(ERROR_D))
//      expectErrors(Row(rowNum, Seq(B_VALID, C_VALID, D_WRONG)), List(ERROR_D))
//
//      expectErrors(Row(rowNum, Seq(B_WRONG, C_EMPTY, D_WRONG)), List(ERROR_B, ERROR_D))
//      expectErrors(Row(rowNum, Seq(B_WRONG, C_VALID, D_WRONG)), List(ERROR_B, ERROR_D))
//    }
//
//    "take external context into account" in {
//      val config: Config = ConfigFactory.parseString(
//        """
//          | {
//          |   fieldInfo: [
//          |     {
//          |       column: "A"
//          |       cellName = "Number in range"
//          |       mandatory: true
//          |       error: [
//          |         {
//          |           id: "custom"
//          |           errorId: "errorId_1"
//          |           errorMsgTemplate: "@{cellName} is not in a range by functions."
//          |           regex: "data >= getMinValue() && data <= getMaxValue()"
//          |         }
//          |         {
//          |           id: "custom.2"
//          |           errorId: "errorId_2"
//          |           errorMsgTemplate: "@{cellName} is not in a range by functions accessed as context bean properties."
//          |           regex: "data >= minValue && data <= maxValue"
//          |         }
//          |       ]
//          |     }
//          |     {
//          |       column: "B"
//          |       cellName = "Optional number"
//          |       mandatory: false
//          |     }
//          |
//          |   ]
//          |
//          |   group-rules:[
//          |    {
//          |      id="A & B ranges"
//          |      errorId="102"
//          |      columns:["A", "B"]
//          |      regex="dataA <= maxValue && dataB >= getMinValue()"
//          |      columnErrors: {
//          |        "A": {errorMsgTemplate = "'@{cellNameA}' or '@{cellNameB}' is out of ranges."}
//          |        "B": {errorMsg = "Check the value!"}
//          |      }
//          |    }
//          |  ]
//          |
//          |   rules: [
//          |     {
//          |       id="MANDATORY"
//          |       errorId="002"
//          |       errorMsgTemplate = "@{cellName} must have an entry."
//          |       regex="!(data == null || data.trim().isEmpty())"
//          |     }
//          |   ]
//          | }
//        """.stripMargin)
//
//      val context = new AnyRef {
//        def getMinValue = 10
//        def getMaxValue = 100
//      }
//
//      val rows: List[List[String]] = List(
//        List("101", "5"), //0 AB
//        List("247", "320"), //1 A
//        List("15", "1"), //2 B
//        List("20", "236") //3
//      )
//
//      expectErrorsInRows(rows, List(
//        ValidationError(Cell("A", 0, "101"), "A & B ranges", "102", "'Number in range' or 'Optional number' is out of ranges."),
//        ValidationError(Cell("B", 0, "5"), "A & B ranges", "102", "Check the value!"),
//
//        ValidationError(Cell("A", 0, "101"), "custom", "errorId_1", "Number in range is not in a range by functions."),
//        ValidationError(Cell("A", 0, "101"), "custom.2", "errorId_2", "Number in range is not in a range by functions accessed as context bean properties."),
//
//        ValidationError(Cell("A", 1, "247"), "A & B ranges", "102", "'Number in range' or 'Optional number' is out of ranges."),
//        ValidationError(Cell("B", 1, "320"), "A & B ranges", "102", "Check the value!"),
//
//        ValidationError(Cell("A", 1, "247"), "custom", "errorId_1", "Number in range is not in a range by functions."),
//        ValidationError(Cell("A", 1, "247"), "custom.2", "errorId_2", "Number in range is not in a range by functions accessed as context bean properties."),
//
//        ValidationError(Cell("A", 2, "15"), "A & B ranges", "102", "'Number in range' or 'Optional number' is out of ranges."),
//        ValidationError(Cell("B", 2, "1"), "A & B ranges", "102", "Check the value!")
//
//      ), config, Some(context), ignoreBlankRows = false)
//
//
//
//      val dataWithEmptyRows: List[List[String]] = List(
//        List("",""),
//        List("101", "5"), //0 AB
//        List("247", "320"), //1 A
//        List("",""),
//        List("15", "1"), //2 B
//        List("20", "236"), //3
//        List("","")
//      )
//
//      expectErrorsInRows(dataWithEmptyRows, List(
//        ValidationError(Cell("A", 1, "101"), "A & B ranges", "102", "'Number in range' or 'Optional number' is out of ranges."),
//        ValidationError(Cell("B", 1, "5"), "A & B ranges", "102", "Check the value!"),
//
//        ValidationError(Cell("A", 1, "101"), "custom", "errorId_1", "Number in range is not in a range by functions."),
//        ValidationError(Cell("A", 1, "101"), "custom.2", "errorId_2", "Number in range is not in a range by functions accessed as context bean properties."),
//
//        ValidationError(Cell("A", 2, "247"), "A & B ranges", "102", "'Number in range' or 'Optional number' is out of ranges."),
//        ValidationError(Cell("B", 2, "320"), "A & B ranges", "102", "Check the value!"),
//
//        ValidationError(Cell("A", 2, "247"), "custom", "errorId_1", "Number in range is not in a range by functions."),
//        ValidationError(Cell("A", 2, "247"), "custom.2", "errorId_2", "Number in range is not in a range by functions accessed as context bean properties."),
//
//        ValidationError(Cell("A", 4, "15"), "A & B ranges", "102", "'Number in range' or 'Optional number' is out of ranges."),
//        ValidationError(Cell("B", 4, "1"), "A & B ranges", "102", "Check the value!")
//
//      ), config, Some(context), ignoreBlankRows = true)
//
//
//    }
//
//
//  }
//
//  "List validation" should {
//    val A_WRONG = "wrong A value"
//    val A_VALID = "Hello, World!"
//
//    val B_WRONG = "It was a damn thing!"
//    val B_VALID = "Valid comment."
//
//    "validate properly" in {
//      val rows: List[List[String]] = List(
//        List(EMPTIES.head, B_VALID), //0
//        List(EMPTIES.last, B_VALID), //1
//        List(A_WRONG, B_VALID), //2
//        List(A_WRONG, B_VALID), //3
//        List(A_VALID, B_WRONG), //4
//        List(A_VALID, B_WRONG), //5
//        List("", B_WRONG), //6
//        List(A_WRONG, B_WRONG) //7
//      )
//
//      val expectedErrors = List(
//        ValidationError(Cell("A", 0, EMPTIES.head), RuleRef.MANDATORY_RULE_REF.id, "002", "Greeting must have an entry."),
//
//        ValidationError(Cell("A", 1, EMPTIES.last), RuleRef.MANDATORY_RULE_REF.id, "002", "Greeting must have an entry."),
//
//        ValidationError(Cell("A", 2, A_WRONG), "custom", "errorId_1", "Greeting is not a helloworld."),
//
//        ValidationError(Cell("A", 3, A_WRONG), "custom", "errorId_1", "Greeting is not a helloworld."),
//
//        ValidationError(Cell("B", 4, B_WRONG), "custom.2", "errorId_2", "Comment must be polite!"),
//
//        ValidationError(Cell("B", 5, B_WRONG), "custom.2", "errorId_2", "Comment must be polite!"),
//
//        ValidationError(Cell("A", 6, ""), RuleRef.MANDATORY_RULE_REF.id, "002", "Greeting must have an entry."),
//        ValidationError(Cell("B", 6, B_WRONG), "custom.2", "errorId_2", "Comment must be polite!"),
//
//        ValidationError(Cell("A", 7, A_WRONG), "custom", "errorId_1", "Greeting is not a helloworld."),
//        ValidationError(Cell("B", 7, B_WRONG), "custom.2", "errorId_2", "Comment must be polite!")
//      )
//
//      expectErrorsInRows(rows, expectedErrors)
//    }
//
//    "pass valid values" in {
//      val rows: List[List[String]] = List(
//        List(A_VALID, B_VALID), //0
//        List(A_VALID, EMPTIES(0)), //1
//        List(A_VALID, EMPTIES(1)), //2
//        List(A_VALID, EMPTIES(2)), //3
//        List(A_VALID, EMPTIES(3)) //4
//      )
//
//      expectOkInRows(rows)
//    }
//
//    "take external context into account" in {
//      val config: Config = ConfigFactory.parseString(
//        """
//          | {
//          |   fieldInfo: [
//          |     {
//          |       column: "A"
//          |       cellName = "Number in range"
//          |       mandatory: true
//          |       error: [
//          |         {
//          |           id: "custom"
//          |           errorId: "errorId_1"
//          |           errorMsgTemplate: "@{cellName} is not in a range by functions."
//          |           regex: "data >= getMinValue() && data <= getMaxValue()"
//          |         }
//          |         {
//          |           id: "custom.2"
//          |           errorId: "errorId_2"
//          |           errorMsgTemplate: "@{cellName} is not in a range by functions accessed as context bean properties."
//          |           regex: "data >= minValue && data <= maxValue"
//          |         }
//          |         {
//          |           id: "custom.3"
//          |           errorId: "errorId_3"
//          |           errorMsgTemplate: "@{cellName} is greater than value accessed as constant with prefix \"get\" in context ."
//          |           regex: "data <= constantMaxValue"
//          |         }
//          |         {
//          |           id: "custom.4"
//          |           errorId: "errorId_4"
//          |           errorMsgTemplate: "@{cellName} is greater than val accessed as function."
//          |           regex: "data <= veryMaxValueVal()"
//          |         }
//          |       ]
//          |     }
//          |   ]
//          |
//          |
//          |   rules: [
//          |     {
//          |       id="MANDATORY"
//          |       errorId="002"
//          |       errorMsgTemplate = "@{cellName} must have an entry."
//          |       regex="!(data == null || data.trim().isEmpty())"
//          |     }
//          |   ]
//          | }
//        """.stripMargin)
//
//      val context = new AnyRef {
//        def getMinValue = 10
//        def getMaxValue = 100
//
//        val getConstantMaxValue = 100 // this can be accessed like bean property: constantMaxValue
//        val veryMaxValueVal = 499 // this can be accessed like function: veryMaxValueVal()
//      }
//
//      val rows: List[List[String]] = List(
//        List("0"), //0
//        List("1"), //1
//        List("9"), //2
//        List("10"), //3
//        List("15"), //4
//        List("99"), //5
//        List("100"), //6
//        List("101"), //7
//        List("500") //8
//      )
//
//      expectErrorsInRows(rows, List(
//        ValidationError(Cell("A", 0, "0"), "custom", "errorId_1", "Number in range is not in a range by functions."),
//        ValidationError(Cell("A", 0, "0"), "custom.2", "errorId_2", "Number in range is not in a range by functions accessed as context bean properties."),
//
//        ValidationError(Cell("A", 1, "1"), "custom", "errorId_1", "Number in range is not in a range by functions."),
//        ValidationError(Cell("A", 1, "1"), "custom.2", "errorId_2", "Number in range is not in a range by functions accessed as context bean properties."),
//
//        ValidationError(Cell("A", 2, "9"), "custom", "errorId_1", "Number in range is not in a range by functions."),
//        ValidationError(Cell("A", 2, "9"), "custom.2", "errorId_2", "Number in range is not in a range by functions accessed as context bean properties."),
//
//        ValidationError(Cell("A", 7, "101"), "custom", "errorId_1", "Number in range is not in a range by functions."),
//        ValidationError(Cell("A", 7, "101"), "custom.2", "errorId_2", "Number in range is not in a range by functions accessed as context bean properties."),
//        ValidationError(Cell("A", 7, "101"), "custom.3", "errorId_3", "Number in range is greater than value accessed as constant with prefix \"get\" in context ."),
//
//        ValidationError(Cell("A", 8, "500"), "custom", "errorId_1", "Number in range is not in a range by functions."),
//        ValidationError(Cell("A", 8, "500"), "custom.2", "errorId_2", "Number in range is not in a range by functions accessed as context bean properties."),
//        ValidationError(Cell("A", 8, "500"), "custom.3", "errorId_3", "Number in range is greater than value accessed as constant with prefix \"get\" in context ."),
//        ValidationError(Cell("A", 8, "500"), "custom.4", "errorId_4", "Number in range is greater than val accessed as function.")
//
//      ), config, Some(context))
//    }
//  }

}
