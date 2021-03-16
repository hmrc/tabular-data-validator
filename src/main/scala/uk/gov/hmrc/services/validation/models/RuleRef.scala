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

package uk.gov.hmrc.services.validation.models

import com.typesafe.config.Config
import uk.gov.hmrc.services.validation.utils.ParseUtils._


/**
 * Reference (by id) to an existing RuleDef.
 * Optionally customisable items are: errorId, errorMsg
 * If expression template in referenced RuleDef has parameters, they should be customised here with parameters map.
 *
 * @param id
 * @param errorId
 * @param errorMsg
 * @param parameters
 */
case class RuleRef(id: String,
                   errorId: Option[String] = None,
                   errorMsg: Option[String] = None,
                   parameters: Option[Map[String, String]] = None)

object RuleRef {

  val RULE_PARAMETERS = "parameters"

  def apply(refConfig: Config): RuleRef = {
    val id = refConfig.getString(RuleDef.RULE_ID)
    val errorId = getStringOpt(refConfig, RuleDef.ERROR_ID)
    val errorMsg = getStringOpt(refConfig, RuleDef.ERROR_MSG)
    val parameters: Option[Map[String, String]] = parseConfigOpt(RULE_PARAMETERS, refConfig)(asMap)

    RuleRef(id = id,
      errorId = errorId,
      errorMsg = errorMsg,
      parameters = parameters)
  }
}
