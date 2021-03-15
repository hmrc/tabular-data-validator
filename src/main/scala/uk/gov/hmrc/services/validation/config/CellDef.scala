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

import com.typesafe.config.Config
import ParseUtils._

case class CellDef(column: String, cellName: String, mandatory: Boolean, rules: List[Rule])

object CellDef {
  val COLUMN = "column"
  val CELL_NAME = "cellName"
  val MANDATORY = "mandatory"
  val RULE_REF = "ruleRef"
  val CELL_RULE = "error"


  def apply(cellConfig: Config, ruleRefResolver: RuleRefResolver): CellDef = {
    val column = cellConfig.getString(COLUMN)
    val cellName = cellConfig.getString(CELL_NAME)
    val manda = cellConfig.getBoolean(MANDATORY)

    //errors mapping
    val cellRules: Option[List[Rule]] =
      parseConfigListOpt(CELL_RULE, cellConfig) {errorConfig => ruleRefResolver.toRule(RuleDef(errorConfig))/*ruleEndorser.toRule(RuleDef(errorConfig))*/}.orElse(Some(Nil))

    //ruleRefs mapping
    val rules: Option[List[Rule]] =
      parseConfigListOpt(RULE_REF, cellConfig) {refConfig => ruleRefResolver.toRule(RuleRef(refConfig))}.orElse(Some(Nil))

    // always add mandatory rule to a cell, further it will be processed by engine depending on cell's mandatory flag
//    val mandaRule: Rule = ruleRefResolver.MANDATORY_RULE

    CellDef(column = column,
      cellName = cellName,
      mandatory = manda,
      rules = /*mandaRule :: */(cellRules.get ::: rules.get)
    )
  }
}
