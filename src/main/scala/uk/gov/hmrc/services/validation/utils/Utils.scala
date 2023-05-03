/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.services.validation.utils

import java.time.format.DateTimeFormatter

import uk.gov.hmrc.services.validation.models.Cell

import scala.util.Try

object Utils {
  def compareCellToRule(regex: Option[String], isDate: Boolean, cellValue: String): Option[Boolean] = {
    if (cellValue.trim.isEmpty) {
      None
    } else if (isDate) {
      Some(Try(DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(cellValue)).isSuccess)
    } else {
      Some(cellValue.matches(regex.get))
    }
  }

  def compareCellsToGroupRule(flagValue: String, cellToCheck: String, dependentCellValue: String): Boolean = {
    if (cellToCheck.matches("^" + flagValue + "$")) dependentCellValue.nonEmpty else true
  }

  def failsMandatoryCheck(isMandatory: Boolean, cell: Cell): Boolean =
    isMandatory && cell.value.trim.isEmpty

}
