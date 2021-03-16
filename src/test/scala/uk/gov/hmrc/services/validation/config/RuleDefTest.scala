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

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.services.validation.models.RuleDef

class RuleDefTest extends WordSpec with Matchers {

  "RuleDef" should {
    "be correctly instantiated from valid config with isDate set to true" in {
      val errorConfig: String =
        """
          |  id = "error.1"
          |  errorMsg = "This is an error message"
          |  errorId = "001"
          |  isDate = true
    """.stripMargin
      val config: Config = ConfigFactory.parseString(errorConfig)
      val ruleDef = RuleDef(config)

      ruleDef.id shouldBe "error.1"
      ruleDef.errorMsg shouldBe "This is an error message"
      ruleDef.errorId shouldBe "001"
      ruleDef.isDate shouldBe Some(true)
      ruleDef.regex shouldBe None
    }

    "be correctly instantiated from valid config with isDate set to false" in {
      val errorConfig: String =
        """
          |  id = "error.2"
          |  errorMsg = "This is another error message"
          |  errorId = "002"
          |  isDate = false
          |  regex = "[0-9a-zA-Z]*"
          |
    """.stripMargin
      val config: Config = ConfigFactory.parseString(errorConfig)
      val ruleDef = RuleDef(config)

      ruleDef.id shouldBe "error.2"
      ruleDef.errorMsg shouldBe "This is another error message"
      ruleDef.errorId shouldBe "002"
      ruleDef.isDate shouldBe Some(false)
      ruleDef.regex shouldBe Some("[0-9a-zA-Z]*")
    }

    "be correctly instantiated from valid config with isDate field missing" in {
      val errorConfig: String =
        """
          |  id = "error.3"
          |  errorMsg = "This is a third error message"
          |  errorId = "003"
          |  regex = "[0-9a-zA-Z]*"
          |
    """.stripMargin
      val config: Config = ConfigFactory.parseString(errorConfig)
      val ruleDef = RuleDef(config)

      ruleDef.id shouldBe "error.3"
      ruleDef.errorMsg shouldBe "This is a third error message"
      ruleDef.errorId shouldBe "003"
      ruleDef.isDate shouldBe None
      ruleDef.regex shouldBe Some("[0-9a-zA-Z]*")
    }

    "fail to be instantiated from invalid config with isDate field set to true and regex present" in {
      val errorConfig: String =
        """
          |  id = "error.2"
          |  errorMsg = "This is another error message"
          |  errorId = "002"
          |  isDate = true
          |  regex = "[0-9a-zA-Z]*"
          |
    """.stripMargin
      val config: Config = ConfigFactory.parseString(errorConfig)
      an[IllegalArgumentException] shouldBe thrownBy(RuleDef(config))
    }

    "fail to be instantiated from invalid config when both the isDate and regex fields are missing" in {
      val errorConfig: String =
        """
          |  id = "error.2"
          |  errorMsg = "This is another error message"
          |  errorId = "002"
          |
    """.stripMargin
      val config: Config = ConfigFactory.parseString(errorConfig)
      an[IllegalArgumentException] shouldBe thrownBy(RuleDef(config))
    }
  }

}