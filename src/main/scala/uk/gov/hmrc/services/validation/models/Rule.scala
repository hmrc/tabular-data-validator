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

case class Rule(id: String,
                errorId: String,
                errorMsg: String,
                isDate: Boolean,
                regex: Option[String]) {
  require(regex.isDefined ^ isDate.equals(true))
}

object Rule {
  val RULE_ID = "id"
  val ERROR_ID = "errorId"
  val DATE_FLAG = "isDate"
  val ERROR_MSG = "errorMsg"
  val REGEX = "regex"

  def apply(ruleConfig: Config): Rule = {
    val id: String = ruleConfig.getString(RULE_ID)
    val errorId: String = ruleConfig.getString(ERROR_ID)
    val isDate: Boolean = Try(ruleConfig.getBoolean(DATE_FLAG)).getOrElse(false)

    val errorMsg: String = ruleConfig.getString(ERROR_MSG)
    val regex: Option[String] = getStringOpt(ruleConfig, REGEX)

    Rule(id = id,
      errorId = errorId,
      isDate = isDate,
      errorMsg = errorMsg,
      regex = regex
    )
  }
}
