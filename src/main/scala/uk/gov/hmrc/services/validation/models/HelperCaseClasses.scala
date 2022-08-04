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


case class Row(rowNum: Int, cells: Seq[Cell]) {
  require(cells.forall(_.row == rowNum))

  val cellsByColumn: Map[String, Cell] = cells.map { c => c.column -> c }.toMap
}

case class Cell(column: String, row: Int, value: String)

case class ValidationError(cell: Cell, ruleId: String, errorId: String, errorMsg: String)

case class GroupRuleFlags(independent: String, dependent: String)

