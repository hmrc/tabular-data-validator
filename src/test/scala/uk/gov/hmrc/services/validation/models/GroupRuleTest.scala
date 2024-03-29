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

package uk.gov.hmrc.services.validation.models

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GroupRuleTest extends AnyWordSpecLike with Matchers {
  def configString(flags: Boolean = true): String =
    s"""{
      |      id="mandatoryCD"
      |      errorId="999"
      |      ${if (flags) """flags: {
      |       independent="C"
      |       dependent="D"
      |      }""".stripMargin else ""}
      |      expectedValue="1.1111"
      |      columnErrors: {
      |        "D": {errorMsg = "Field must have an entry."}
      |      }
      |}""".stripMargin

  val configParsed: Config = ConfigFactory.parseString(configString())

  "GroupRule" must {
    "parse a group-rules config properly" in {
      val groupRule: GroupRule = GroupRule(configParsed)
      groupRule shouldBe GroupRule(
        "mandatoryCD",
        "999",
        Map("D" -> "Field must have an entry."),
        GroupRuleFlags("C", "D"),
        "1.1111"
      )
    }
    "not parse if given config without flags" in {
      val configIllegal: Config = ConfigFactory.parseString(configString(flags = false))
      intercept[ConfigException](GroupRule(configIllegal)).getMessage shouldBe "String: 1: No configuration setting found for key 'flags'"
    }
  }
}
