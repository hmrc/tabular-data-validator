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
import uk.gov.hmrc.services.validation.Utils


class ValidationConfig(validationConfig: Config) extends RuleRefResolver {

//  val script: Option[String] = getStringListOpt(validationConfig, "definitions.script").map(_.mkString("\n"))

//  script.map(Utils.compileExpression(_)) // to make sure it's compilable

  val ruleDefs: Map[String, RuleDef] = parseConfigList("rules", validationConfig){RuleDef(_)}.map(r => (r.id, r)).toMap
//  val MANDATORY_RULE: Rule = toRule(RuleRef.MANDATORY_RULE_REF)

  val cells: List[CellDef] = parseConfigList("fieldInfo", validationConfig){CellDef(_, ruleRefResolver = this)}
  val cellsByColumn: Map[String, CellDef] = cells.map(c => c.column -> c).toMap
//  val cellsByName: Map[String, CellDef] = cells.map(c => c.cellName -> c).toMap

  val groupRules: Option[List[GroupRule]] = parseConfigListOpt("group-rules", validationConfig){GroupRule(_)}

}

trait RuleRefResolver {
  def ruleDefs: Map[String, RuleDef]
//  def script: Option[String]
//  def MANDATORY_RULE: Rule

  def toRule(ruleRef: RuleRef): Rule = {
    val ruleDef = ruleDefs(ruleRef.id)
    Rule(ruleDef, Some(ruleRef))
  }

  def toRule(ruleDef: RuleDef): Rule = {
    Rule(ruleDef, None)
  }
}
