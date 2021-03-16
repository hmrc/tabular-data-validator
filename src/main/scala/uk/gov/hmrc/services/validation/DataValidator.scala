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

import com.typesafe.config.Config
import uk.gov.hmrc.services.validation.config.ValidationConfig
import uk.gov.hmrc.services.validation.models.{CellDefinition, Rule}
import uk.gov.hmrc.services.validation.models._

import scala.util.{Success, Failure, Try}

class DataValidator(validationConfig: Config) {

  val cfg = new ValidationConfig(validationConfig)

  def validateCell(cell: Cell): Option[ValidationError] = {

    val celldef: CellDefinition = cfg.cellsByColumn(cell.column)
    val rule: Rule = celldef.rule
    val failedMandatoryCheck: Boolean = Utils.failsMandatoryCheck(celldef.mandatory, cell)
    val cellIsValid: Option[Boolean] = Utils.compareCellToRule(rule.regex, rule.isDate, cell.value)

    def getValidationError: Some[ValidationError] = Some(ValidationError(cell, rule.id, rule.errorId, rule.errorMsg))

    cellIsValid match {
      case Some(true) => None
      case Some(false) => getValidationError
      case None => if (failedMandatoryCheck) getValidationError else None
    }
  }

  def validateRow(row: Row): Option[List[ValidationError]] = {

    val paddedCells: Seq[Cell] = cfg.cells.map { cell =>
      Try {
        row.cellsByColumn(cell.column)
      } match {
        case Success(cell) => cell
        case Failure(_) => MissingCell(cell.column, row.rowNum).toCell
      }
    }

    val groupErrors: Seq[ValidationError] = cfg.groupRules.getOrElse(Nil).flatMap { rule =>
      def cellValue(column: String): String = {
        paddedCells.find(_.column == column).getOrElse(
          throw new RuntimeException("[DataValidator][validateRow] The columns defined in fieldInfo did not have a definition for one of the columns in group-rules.")
        ).value
      }

      val ruleResult: Boolean = Utils.compareCellsToGroupRule(rule.expectedValue, cellValue(rule.flags.independent), cellValue(rule.flags.dependent))
      if (!ruleResult) {

        paddedCells.filter(cell => rule.columnErrors.contains(cell.column)).map {
          cell =>
            ValidationError(cell, rule.id, rule.errorId, rule.columnErrors(cell.column))
        }
      } else Seq.empty

    }

    val errorsFromCells: Seq[ValidationError] = paddedCells.flatMap {
      cell => validateCell(cell)
    }

    val allErrors: Seq[ValidationError] = groupErrors ++ errorsFromCells

    if (allErrors.nonEmpty) Some(allErrors.toList) else None
  }

}
