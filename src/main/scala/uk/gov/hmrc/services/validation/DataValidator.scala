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
import uk.gov.hmrc.services.validation.config.ValidationConfig
import uk.gov.hmrc.services.validation.models.{CellDefinition, Rule}
import uk.gov.hmrc.services.validation.models._

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.{Map => MutableMap}
import scala.util.{Success, Failure, Try}


trait DataValidator {
  /**
   *
   * @param rows            - the data to validate
   * @param firstRowNum     - all errors contain the row number... specify the staring number here.
   * @param ignoreBlankRows - shall we ignore completely blank rows?
   * @return - None or Some List of Validation errors
   */
  def validateRows(rows: List[List[String]], firstRowNum: Int, ignoreBlankRows: Boolean): Option[List[ValidationError]]

  /**
   *
   * @param rows            - the data to validate
   * @param errorBuffer     - a ListBuffer you wish to collate your errors in
   * @param firstRowNum     - all errors contain the row number... specify the staring number here.
   * @param ignoreBlankRows - shall we ignore completely blank rows?
   */
  def validateRowsBuffered(rows: List[List[String]], errorBuffer: ListBuffer[ValidationError], firstRowNum: Int, ignoreBlankRows: Boolean)

  /**
   *
   * @param rows      - the data to validate
   * @param zeroBased - will the first row be treated as 0 or 1?
   * @return - None or Some List of Validation errors
   */
  def validateRows(rows: List[List[String]], zeroBased: Boolean = true): Option[List[ValidationError]]

  /**
   *
   * @param row         - The rows you wish to validate
   * @param errorBuffer - a ListBuffer you wish to collate your errors in
   */
  def validateRowBuffered(row: Row, errorBuffer: ListBuffer[ValidationError]): Unit

  /**
   *
   * @param cell - The cell you wish to validate
   * @return - None or Some with a List of all the errors
   */
  def validateCell(cell: Cell): Option[ValidationError]

  /**
   *
   * @param row - The row you wish to validate
   * @return - None or Some with a List of all the errors
   */
  def validateRow(row: Row): Option[List[ValidationError]]

}

object DataValidator {

  /**
   * Create a DataValidator based on the supplied configuration
   *
   * @param validationConfig
   * @return
   */
  def apply(validationConfig: Config): DataValidator = new DataValidatorImpl(validationConfig)

  ///////////////////////////////////////////////////////////////////////
  private class DataValidatorImpl(validationConfig: Config) extends DataValidator {

    val cfg = new ValidationConfig(validationConfig)

    def validateRows(rows: List[List[String]], zeroBased: Boolean = true): Option[List[ValidationError]] = {
      val firstRowNum = if (zeroBased) 0 else 1
      validateRows(rows, firstRowNum, false)
    }

    /**
     *
     * @param rows            - the data to validate
     * @param firstRowNum     - all errors contain the row number... specify the staring number here.
     * @param ignoreBlankRows - shall we ignore completely blank rows?
     * @return - None or Some List of Validation errors
     */
    def validateRows(rows: List[List[String]], firstRowNum: Int, ignoreBlankRows: Boolean): Option[List[ValidationError]] = {

      val errorBuffer: ListBuffer[ValidationError] = ListBuffer()

      validateRowsBuffered(rows, errorBuffer, firstRowNum, ignoreBlankRows)

      if (errorBuffer.length == 0) {
        None
      } else {
        Some(errorBuffer.toList)
      }
    }

    /**
     *
     * @param rows            - the data to validate
     * @param errorBuffer     - a ListBuffer you wish to collate your errors in
     * @param firstRowNum     - all errors contain the row number... specify the staring number here.
     * @param ignoreBlankRows - shall we ignore completely blank rows?
     */
    override def validateRowsBuffered(rows: List[List[String]], errorBuffer: ListBuffer[ValidationError], firstRowNum: Int, ignoreBlankRows: Boolean): Unit = {

      val colIndexCache: MutableMap[Int, String] = MutableMap()

      def columnIndex2Letters(columnIndex: Int): String = {
        if (columnIndex < 0) throw new IllegalArgumentException(s"Column index should not be negative: $columnIndex")

        @tailrec
        def index2letter(index: Int, prevLetters: List[Char]): List[Char] = {
          val NUM_OF_LETTERS = 26

          def letter(index: Int): Char =
            if (index >= NUM_OF_LETTERS || index < 0) throw new IllegalArgumentException(s"Wrong letter index: $index")
            else ('A'.toInt + index).toChar

          val (divisor, remainder) = index / NUM_OF_LETTERS -> index % NUM_OF_LETTERS
          val letters = letter(remainder) :: prevLetters
          if (divisor == 0) {
            letters
          } else {
            index2letter(divisor - 1, letters)
          }
        }

        if (colIndexCache.contains(columnIndex)) {
          colIndexCache(columnIndex)
        } else {
          val newIndex = index2letter(columnIndex, Nil).mkString
          colIndexCache += (columnIndex -> newIndex)
          newIndex
        }
      }

      rows.foldLeft(firstRowNum) { case (rowNum, row: List[String]) =>
        val cells: Seq[Cell] = row.zipWithIndex.map { case (data, index) => Cell(columnIndex2Letters(index), rowNum, data) }

        val populatedRow = cells.exists(cell => cell.value.nonEmpty)

        if (populatedRow || (!populatedRow && !ignoreBlankRows)) {
          validateRowBuffered(Row(rowNum, cells), errorBuffer)
        }
        rowNum + 1
      }
    }

    override def validateCell(cell: Cell): Option[ValidationError] = {

      val celldef: CellDefinition = cfg.cellsByColumn(cell.column)
      val rule: Rule = celldef.rule
      val failedMandatoryCheck: Boolean = Utils.failsMandatoryCheck(celldef.mandatory, cell)
      val cellIsValid: Boolean = Utils.compareCellToRule(rule.regex, rule.isDate, cell.value)

      if (!cellIsValid || failedMandatoryCheck) {
        val errorMsg = rule.errorMsg match {
          case Right(errorMsg) => errorMsg
          case Left(templateErrorMsg) =>
            val msgmap: Map[String, String] = Map("cellName" -> celldef.cellName, "column" -> celldef.column) ++
              rule.parameters.getOrElse(Map())
            Utils.parseTemplate(templateErrorMsg, msgmap)
        }
        Some(ValidationError(cell, rule.id, rule.errorId, errorMsg))
      } else None
    }

    override def validateRowBuffered(row: Row, errors: ListBuffer[ValidationError]): Unit = {

      for {rule <- cfg.groupRules.getOrElse(Nil)
           if rule.columns.forall(column => row.cellsByColumn.contains(column))} {

        val columns: Set[String] = rule.columns
        val cells: Set[Either[MissingCell, Cell]] = columns.map {
          column =>
            Try {
              row.cellsByColumn(column)
            } match {
              case Success(cell) => Right(cell)
              case Failure(_) => Left(MissingCell(column, row.rowNum))
            }
        }

        val celldefs: Set[CellDefinition] = columns.map {
          cfg.cellsByColumn(_)
        } // todo WHAT IF COLUMNDEF DOES NOT EXIST ?

        val ruleResult: Boolean = Utils.compareCellsToGroupRule(rule.expectedValue, rule.flags.independent, rule.flags.dependent)
        if (!ruleResult) {
          // validation fails - create error results
          val msgmap: Map[String, String] = Map("row" -> row.rowNum.toString) ++
            celldefs.foldLeft(Map[String, String]()) { (map, celldef) =>
              map + (s"cellName${celldef.column}" -> celldef.cellName)
            }

          cells.foreach {
            maybeMissingCell =>
              val cell: Cell = maybeMissingCell match {
                case validCell: Cell => validCell
                case missingCell: MissingCell => missingCell.toCell
              }
              rule.isTemplateErrorMsgFor(cell.column).map {
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

      val errorsFromCells: Seq[ValidationError] = row.cells.flatMap {
        cell => validateCell(cell)
      }

      errors ++= errorsFromCells
    }

    override def validateRow(row: Row): Option[List[ValidationError]] = {

      val errors = ListBuffer[ValidationError]()

      validateRowBuffered(row, errors)

      if (errors.length > 0) {
        Some(errors.toList)
      } else {
        None
      }
    }
  }

}
