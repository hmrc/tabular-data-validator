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

/**
 * This is the actual rule to apply.
 * Not intended to be configured in a file, created internally instead from a given RuleDef.
 *
 * @param id
 * @param errorId
 * @param errorMsg
 * @param regex
 */
case class Rule(id: String,
                errorId: String,
                errorMsg: String,
                isDate: Boolean,
                regex: Option[String])

object Rule {

  def apply(ruleDef: RuleDef): Rule = {

    val errorId: String = ruleDef.errorId
    val errorMsg: String = ruleDef.errorMsg
    val isDateFlag: Boolean = ruleDef.isDate.contains(true)

    Rule(id = ruleDef.id,
      errorId = errorId,
      errorMsg = errorMsg,
      isDate = isDateFlag,
      regex = ruleDef.regex
    )

  }

}
