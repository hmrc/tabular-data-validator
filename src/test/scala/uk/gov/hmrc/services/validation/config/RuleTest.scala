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

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.services.validation.Utils
import uk.gov.hmrc.services.validation.models.{RuleDef, Rule, RuleRef}

class RuleTest extends WordSpec with Matchers {

  val ID = "Some ID"
  val ERROR_ID: String = "Some error id"
  val MSG: String = "Some error msg"
  val ourRegex: String = "[0-9]*"

  "Rule" should {
    "be created from a given RuleDef" in {
      val ruleDef: RuleDef = RuleDef(id = ID, errorId = ERROR_ID, errorMsg = MSG, regex = Some(ourRegex), isDate = Some(false))

      val rule = Rule(ruleDef, ruleRefOpt = None)
      rule.id shouldBe(ruleDef.id)
      rule.errorId shouldBe(ruleDef.errorId)
      rule.errorMsg shouldBe(ruleDef.errorMsg)
      rule.parameters shouldBe(None)
      rule.regex shouldBe ruleDef.regex
    }

  }

}
