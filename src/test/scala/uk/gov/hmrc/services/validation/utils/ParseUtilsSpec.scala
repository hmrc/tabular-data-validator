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

package uk.gov.hmrc.services.validation.utils

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.services.validation.models.{CellDefinition, Rule, RuleDef}

import scala.util.{Failure, Success}

class ParseUtilsSpec extends WordSpec with Matchers {

  "In ParseUtils" when {
    val parser = ParseUtils

    "getStringOpt is called" should {
      "optionally retrieve a string from config" in {
        val configString: String =
          """
            |  id = "config string"
        """.stripMargin
        val config: Config = ConfigFactory.parseString(configString)
        parser.getStringOpt(config, "id") shouldBe Some("config string")
        parser.getStringOpt(config, "nothing") shouldBe None
      }
    }

    "getStringSet is called" should {
      "return a Set with the config list values" in {
        val configString: String =
          """
            |  listThing: [
            |   config string
            |   true
            |   false
            |   true
            |   yes
            |   yes
            |   no
            |   ]
        """.stripMargin
        val config: Config = ConfigFactory.parseString(configString)
        parser.getStringSet(config, "listThing") shouldBe Set("yes", "no", "true", "false", "config string")
      }
    }

    "getTryGroupRuleFlags is called" should {

      "return success with group rule flags when they are present" in {
        val configString: String =
          """
            |  {
            |        id="mandatoryC"
            |        errorId="C01"
            |        columns:["C", "B"]
            |        expectedValue = "1.1111"
            |        flags: {
            |            independent = "B"
            |            dependent = "C"
            |        }
            |        columnErrors: {
            |          "C":  {errorMsgTemplate = ${validation-types.srn.errorMsg}}
            |        }
            |      }
        """.stripMargin
        val config: Config = ConfigFactory.parseString(configString)
        val result = parser.getTryGroupRuleFlags(config, "flags")
        result match {
          case Success(output) =>
            output.dependent shouldBe "C"
            output.independent shouldBe "B"
          case Failure(_) => fail("Failed to parse config for flags")
        }
      }

      "return failure when group rule flags are missing" in {
        val configString: String =
          """
            |  {
            |        id="mandatoryC"
            |        errorId="C01"
            |        columns:["C", "B"]
            |        expectedValue = "1.1111"
            |        columnErrors: {
            |          "C":  {errorMsgTemplate = ${validation-types.srn.errorMsg}}
            |        }
            |      }
        """.stripMargin
        val config: Config = ConfigFactory.parseString(configString)
        val result = parser.getTryGroupRuleFlags(config, "flags")
        result match {
          case Success(_) =>
            fail("How did this find the config?")
          case Failure(_) => succeed
        }
      }

      "return failure when one of the group rule flags are missing" in {
        val configString: String =
          """
            |  {
            |        id="mandatoryC"
            |        errorId="C01"
            |        columns:["C", "B"]
            |        expectedValue = "1.1111"
            |        flags: {
            |          independent = "B"
            |        }
            |        columnErrors: {
            |          "C":  {errorMsgTemplate = ${validation-types.srn.errorMsg}}
            |        }
            |      }
        """.stripMargin
        val config: Config = ConfigFactory.parseString(configString)
        val result = parser.getTryGroupRuleFlags(config, "flags")
        result match {
          case Success(_) =>
            fail("How did this find the config?")
          case Failure(_) => succeed
        }
      }
    }

    "getConfigList is called" should {

      "getConfigList will return a list of config objects with the config values" in {
        val configString: String =
          """
            |  listThing: [
            |     {
            |     name = "config string"
            |     bool = true
            |     }
            |   ]
        """.stripMargin
        val config: Config = ConfigFactory.parseString(configString)
        parser.getConfigList("listThing", config).toString shouldBe """List(Config(SimpleConfigObject({"bool":true,"name":"config string"})))"""
      }
    }

    "parseConfigList is called" should {

      "parse config list into a case class" in {
        val configString: String =
          """
            |  fieldInfo : [
            |  {
            |      column = "D"
            |      cellName = "Name"
            |      mandatory = false
            |      error: {
            |          id = "error.4"
            |          errorMsg = "message"
            |          validationID = "4"
            |          regex = "[0-9]{1,11}\\.[0-9]{2}"
            |          errorId = "004"
            |        }
            |    }
            |    ]
        """.stripMargin
        val config: Config = ConfigFactory.parseString(configString)
        parser.parseConfigList("fieldInfo", config)(CellDefinition(_)) shouldBe
          List(CellDefinition("D", "Name", false, Rule("error.4", "004", "message", None, false, Some("[0-9]{1,11}\\.[0-9]{2}"))))
      }
    }

    "parseConfigListOpt is called" should {

      "return None if path is not valid" in {
        val configString: String =
          """
            |  fieldInfo : [
            |  {
            |      column = "D"
            |      cellName = "Name"
            |      mandatory = false
            |      error: {
            |          id = "error.4"
            |          errorMsg = "message"
            |          validationID = "4"
            |          regex = "[0-9]{1,11}\\.[0-9]{2}"
            |          errorId = "004"
            |        }
            |    }
            |    ]
        """.stripMargin
        val config: Config = ConfigFactory.parseString(configString)
        parser.parseConfigListOpt("fieldInfo", config)(CellDefinition(_)) shouldBe
          Some(List(CellDefinition("D", "Name", false, Rule("error.4", "004", "message", None, false, Some("[0-9]{1,11}\\.[0-9]{2}")))))
        parser.parseConfigListOpt("nope", config)(CellDefinition(_)) shouldBe None
      }
    }

    "parseConfig is called" should {

      "parse config object into case class" in {
        val configString: String =
          """
            |  object :
            |  {
            |    id = "error.4"
            |    errorMsg = "message"
            |    validationID = "4"
            |    regex = "[0-9]{1,11}\\.[0-9]{2}"
            |    errorId = "004"
            |  }
        """.stripMargin
        val config: Config = ConfigFactory.parseString(configString)
        parser.parseConfig("object", config) { errorConfig => Rule(RuleDef(errorConfig), None) } shouldBe
          Rule("error.4", "004", "message", None, false, Some("[0-9]{1,11}\\.[0-9]{2}"))
      }
    }

    "parseConfigOpt is called" should {

      "return none if path is not valid" in {
        val configString: String =
          """
            |  object :
            |  {
            |    id = "error.4"
            |    errorMsg = "message"
            |    validationID = "4"
            |    regex = "[0-9]{1,11}\\.[0-9]{2}"
            |    errorId = "004"
            |  }
        """.stripMargin
        val config: Config = ConfigFactory.parseString(configString)
        parser.parseConfigOpt("object", config) { errorConfig => Rule(RuleDef(errorConfig), None) } shouldBe
          Some(Rule("error.4", "004", "message", None, false, Some("[0-9]{1,11}\\.[0-9]{2}")))
        parser.parseConfigOpt("invalid", config) { errorConfig => Rule(RuleDef(errorConfig), None) } shouldBe None
      }
    }
  }
}
