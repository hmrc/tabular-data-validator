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
import uk.gov.hmrc.services.validation.models.{RuleDef, RuleRef}

class RuleRefTest extends WordSpec with Matchers {

  "RuleRef" should {
    "be correctly instantiated from valid config" in {
      val errorConfig: String =
        """
          |  id = "error.1"
          |  errorMsg = "This is an error message"
          |  errorId = "001"
          |  isDate = true
    """.stripMargin
      val config: Config = ConfigFactory.parseString(errorConfig)
      val ruleRef = RuleRef(config)

      ruleRef.id shouldBe "error.1"
    }
  }
}

//TODO RuleRefs are never used, can be removed once the reset of the tests are done