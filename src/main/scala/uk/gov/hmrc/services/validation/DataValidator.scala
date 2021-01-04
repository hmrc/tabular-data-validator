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
import uk.gov.hmrc.services.validation.config.{ValidationConfig, RuleRef, Rule, CellDef}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.{ Map => MutableMap }


trait DataValidator {
  /**
   *
   * @param rows              - the data to validate
   * @param contextObjectOpt  - a helper object to support the validation rules
   * @param firstRowNum       - all errors contain the row number... specify the staring number here.
   * @param ignoreBlankRows   - shall we ignore completely blank rows?
   * @return                  - None or Some List of Validation errors
   */
  def validateRows(rows: List[List[String]], contextObjectOpt: Option[AnyRef], firstRowNum : Int, ignoreBlankRows : Boolean): Option[List[ValidationError]]

  /**
   *
   * @param rows              - the data to validate
   * @param errorBuffer            - a ListBuffer you wish to collate your errors in
   * @param contextObjectOpt  - a helper object to support the validation rules
   * @param firstRowNum       - all errors contain the row number... specify the staring number here.
   * @param ignoreBlankRows   - shall we ignore completely blank rows?
   */
  def validateRowsBuffered(rows: List[List[String]], errorBuffer : ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef], firstRowNum : Int, ignoreBlankRows : Boolean)

  /**
   *
   * @param rows              - the data to validate
   * @param contextObjectOpt  - a helper object to support the validation rules
   * @param zeroBased         - will the first row be treated as 0 or 1?
   * @return                  - None or Some List of Validation errors
   */
  def validateRows(rows: List[List[String]], contextObjectOpt: Option[AnyRef], zeroBased: Boolean = true) : Option[List[ValidationError]]

  /**
   *
   * @param cell             - The cell you wish to validate
   * @param errorBuffer      - a ListBuffer you wish to collate your errors in
   * @param contextObjectOpt - a helper object to support the validation rules
   */
  def validateCellBuffered(cell: Cell, errorBuffer : ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef] = None) : Unit

  /**
   *
   * @param row               - The rows you wish to validate
   * @param errorBuffer       - a ListBuffer you wish to collate your errors in
   * @param contextObjectOpt - a helper object to support the validation rules
   */
  def validateRowBuffered(row: Row, errorBuffer : ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef] = None) : Unit

  /**
   *
   * @param cell              - The cell you wish to validate
   * @param contextObjectOpt - a helper object to support the validation rules
   * @return                  - None or Some with a List of all the errors
   */
  def validateCell(cell: Cell, contextObjectOpt: Option[AnyRef] = None): Option[List[ValidationError]]

  /**
   *
   * @param row               - The row you wish to validate
   * @param contextObjectOpt  - a helper object to support the validation rules
   * @return                  - None or Some with a List of all the errors
   */
  def validateRow(row: Row, contextObjectOpt: Option[AnyRef] = None): Option[List[ValidationError]]

}

case class Row(rowNum: Int, cells: Seq[Cell]) {
  require(cells.forall(_.row == rowNum))

  val cellsByColumn: Map[String, Cell] = cells.map{c => c.column -> c}.toMap
}

case class Cell(column: String, row: Int, value: String)

case class ValidationError(cell: Cell, ruleId: String, errorId: String, errorMsg: String)

object DataValidator {

  private lazy val defaultValidator = apply(ConfigFactory.load.getConfig("validation-config"))

  /**
   *  Create a DataValidator based on the default configuration in file validation-config.conf
   * @return
   */
  def apply(): DataValidator = defaultValidator

  /**
   *  Create a DataValidator based on the supplied configuration
   * @param validationConfig
   * @return
   */
  def apply(validationConfig: Config): DataValidator = new DataValidatorImpl(validationConfig)

  @Deprecated // validateRows left in for compatability... Deprecated
  def validateRows(rows : List[List[String]], validationConfig :Config, contextObjectOpt : Option[AnyRef], zeroBased : Boolean = true) : Option[List[ValidationError]] = {
    DataValidator(validationConfig).validateRows(rows, contextObjectOpt, zeroBased)
  }

  @Deprecated // validateRows left in for compatability... Deprecated
  def validateRows(rows :List[List[String]], validationConfig : Config, contextObjectOpt : Option[AnyRef], firstRowNum : Int, ignoreBlankRows : Boolean) : Option[List[ValidationError]] = {
    DataValidator(validationConfig).validateRows(rows, contextObjectOpt, firstRowNum, ignoreBlankRows)
  }


  ///////////////////////////////////////////////////////////////////////
  private class DataValidatorImpl(validationConfig: Config) extends DataValidator {

    val cfg = new ValidationConfig(validationConfig)

    def validateRows(rows: List[List[String]], contextObjectOpt: Option[AnyRef], zeroBased: Boolean = true) : Option[List[ValidationError]] = {
      val firstRowNum = if (zeroBased) 0 else 1
      validateRows(rows, contextObjectOpt, firstRowNum, false)
    }

    /**
     *
     * @param rows              - the data to validate
     * @param contextObjectOpt  - a helper object to support the validation rules
     * @param firstRowNum       - all errors contain the row number... specify the staring number here.
     * @param ignoreBlankRows   - shall we ignore completely blank rows?
     * @return                  - None or Some List of Validation errors
     */
    def validateRows(rows: List[List[String]], contextObjectOpt: Option[AnyRef], firstRowNum : Int, ignoreBlankRows : Boolean): Option[List[ValidationError]] = {

      val errorBuffer: ListBuffer[ValidationError] = ListBuffer()

      validateRowsBuffered(rows, errorBuffer, contextObjectOpt, firstRowNum, ignoreBlankRows)

      if (errorBuffer.length == 0) {
        None
      } else {
        Some(errorBuffer.toList)
      }
    }

    /**
     *
     * @param rows              - the data to validate
     * @param errorBuffer       - a ListBuffer you wish to collate your errors in
     * @param contextObjectOpt  - a helper object to support the validation rules
     * @param firstRowNum       - all errors contain the row number... specify the staring number here.
     * @param ignoreBlankRows   - shall we ignore completely blank rows?
     */
    override def validateRowsBuffered(rows: List[List[String]], errorBuffer: ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef], firstRowNum: Int, ignoreBlankRows: Boolean): Unit = {

      val colIndexCache : MutableMap[Int,String] = MutableMap()

      def columnIndex2Letters(columnIndex: Int): String = {
        if (columnIndex < 0) throw new IllegalArgumentException(s"Column index should not be negative: $columnIndex")

        @tailrec
        def index2letter(index: Int, prevLetters: List[Char]): List[Char] = {
          val NUM_OF_LETTERS = 26
          def letter(index: Int): Char =
            if (index >= NUM_OF_LETTERS || index < 0 ) throw new IllegalArgumentException(s"Wrong letter index: $index")
            else ('A'.toInt + index).toChar

          val (divisor, remainder) = index / NUM_OF_LETTERS -> index % NUM_OF_LETTERS
          val letters = letter(remainder) :: prevLetters
          if (divisor == 0) {
            letters
          } else {
            index2letter (divisor - 1, letters)
          }
        }

        if (colIndexCache.contains(columnIndex)) {
          colIndexCache(columnIndex)
        } else {
          val newIndex =  index2letter(columnIndex, Nil).mkString
          colIndexCache += ( columnIndex -> newIndex)
          newIndex
        }
      }

      rows.foldLeft(firstRowNum){ case (rowNum, row: List[String]) =>
        val cells: Seq[Cell] = row.zipWithIndex.map{case (data, index) => Cell(columnIndex2Letters(index), rowNum, data)}

        val populatedRow = cells.exists(cell => cell.value.nonEmpty)

        if (populatedRow || (!populatedRow && !ignoreBlankRows)) {
          validateRowBuffered(Row(rowNum, cells), errorBuffer, contextObjectOpt)
        }
        rowNum + 1
      }
    }

    override def validateCellBuffered(cell: Cell, errors : ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef] ) : Unit = {

      val celldef: CellDef = cfg.cellsByColumn(cell.column)
      val rules: List[Rule] = celldef.rules
      val datamap: Map[String, String] = Map("data" -> cell.value)

      for (rule <- rules) {
        val ruleresult: Option[Any] = Utils.executeExpression(rule.compiledExpr, contextObjectOpt, datamap)
        if (ruleresult != Some(true)) {
          // validation fails - create error result
          val errorMsg =
            if (rule.isTemplateErrorMsg) {
              val template = rule.errorMsg.left.get
              val msgmap: Map[String, String] = Map("cellName" -> celldef.cellName, "column" -> celldef.column) ++
                rule.parameters.getOrElse(Map())
              Utils.parseTemplate(template, msgmap)
            } else {
              rule.errorMsg.right.get
            }
          val error = ValidationError(cell, rule.id, rule.errorId, errorMsg)

          if (rule.id == RuleRef.MANDATORY_RULE_REF.id) {
            // mandatory failed
            if(celldef.mandatory) {
              // append the mandatory error to the rest of results
              errors += error
            }
            return  // immediate return on mandatory cell violation
          } else {
            errors += error
          }
        }
      }
    }

    override def validateRowBuffered(row: Row, errors : ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef] ) : Unit = {

      for {rule <- cfg.groupRules.getOrElse(Nil)
          if rule.columns.forall(row.cellsByColumn.contains)} {
        val columns = rule.columns
        val cells: Set[Cell] = columns.map { column => row.cellsByColumn(column)} // todo WHAT IF COLUMN DOES NOT EXIST IN ROW?
        val celldefs: Set[CellDef] = columns.map{cfg.cellsByColumn(_)} // todo WHAT IF COLUMNDEF DOES NOT EXIST ?
        val datamap: Map[String, String] = cells.foldLeft(Map[String, String]()) { case (map, cell) =>
            map + (s"data${cell.column}" -> cell.value)
          }

        val ruleresult: Option[Any] = Utils.executeExpression(rule.compiledExpr, contextObjectOpt, datamap)
        if (ruleresult != Some(true)) {
          // validation fails - create error results
          val msgmap: Map[String, String] = Map("row" -> row.rowNum.toString) ++
            celldefs.foldLeft(Map[String, String]()){ (map, celldef) =>
              map + (s"cellName${celldef.column}" -> celldef.cellName)}

          cells.foreach  {  cell =>
            rule.isTemplateErrorMsgFor(cell.column).map{
              case true =>
                val template = rule.columnErrors(cell.column).left.get
                Utils.parseTemplate(template, msgmap)

              case false => rule.columnErrors(cell.column).right.get
            }
              .collect {
              case errorMsg: String => errors += ValidationError(cell, rule.id, rule.errorId, errorMsg)
            }
          }
        }
      }

      row.cells.foreach{
        cell =>  validateCellBuffered(cell, errors, contextObjectOpt)
      }
    }

    override def validateCell(cell: Cell, contextObjectOpt: Option[AnyRef]): Option[List[ValidationError]] = {
      val errors = ListBuffer[ValidationError]()

      validateCellBuffered(cell: Cell, errors : ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef] )

      if (errors.length > 0 ) {
        Some(errors.toList)
      } else {
        None
      }
    }

    override def validateRow(row: Row, contextObjectOpt: Option[AnyRef]): Option[List[ValidationError]] = {

      val errors = ListBuffer[ValidationError]()

      validateRowBuffered(row, errors , contextObjectOpt)

      if (errors.length > 0 ) {
        Some(errors.toList)
      } else {
        None
      }
    }
  }
}
