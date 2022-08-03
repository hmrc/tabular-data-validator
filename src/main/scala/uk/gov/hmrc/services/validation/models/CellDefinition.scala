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

import com.typesafe.config.Config
import uk.gov.hmrc.services.validation.utils.ParseUtils.parseConfig

case class CellDefinition(column: String, cellName: String, mandatory: Boolean, rule: Rule)

object CellDefinition {
  val COLUMN = "column"
  val CELL_NAME = "cellName"
  val MANDATORY = "mandatory"
  val CELL_RULE = "error"

  def apply(cellConfig: Config): CellDefinition = {
    val column = cellConfig.getString(COLUMN)
    val cellName = cellConfig.getString(CELL_NAME)
    val manda = cellConfig.getBoolean(MANDATORY)

    val cellRule: Rule = parseConfig(CELL_RULE, cellConfig) {errorConfig => Rule(errorConfig)}

    CellDefinition(column = column,
      cellName = cellName,
      mandatory = manda,
      rule = cellRule
    )
  }
}
