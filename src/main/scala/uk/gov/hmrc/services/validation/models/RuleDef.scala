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

import scala.util.Try

/**
 * This is a definition, template of the rule.
 * From that definition the actual rule should be created.
 *
 * @param id
 * @param errorId
 * @param errorMsg
 * @param regex
 */
case class RuleDef(id: String, errorId: String, errorMsg: String,
                   regex: Option[String], isDate: Option[Boolean]) {

  require(regex.isDefined ^ isDate.contains(true))

}

object RuleDef {
  val RULE_ID = "id"
  val ERROR_ID = "errorId"
  val DATE_FLAG = "isDate"
  val ERROR_MSG = "errorMsg"
  val REGEX = "regex"

  def apply(ruleConfig: Config): RuleDef = {
    val id = ruleConfig.getString(RULE_ID)
    val errorId = ruleConfig.getString(ERROR_ID)
    val isDate = Try(ruleConfig.getBoolean(DATE_FLAG)).toOption

    val errorMsg = ruleConfig.getString(ERROR_MSG)
    val regex = getStringOpt(ruleConfig, REGEX)

    RuleDef(id = id,
      errorId = errorId,
      isDate = isDate,
      errorMsg = errorMsg,
      regex = regex
    )
  }
}
