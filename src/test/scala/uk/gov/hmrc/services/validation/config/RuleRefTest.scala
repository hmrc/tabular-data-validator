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

import java.io.StringReader

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}

/**
 * Created by user02 on 10/10/14.
 */
class RuleRefTest extends WordSpec with Matchers {

  implicit def string2option(s: String): Option[String] = Some(s)

  val ID = "Some ID"
  val ERROR_ID = "Some error id"
  val MSG = "Some error msg"
  val MSG_TEMPLATE = "Some error msg template"
  val PARAMS: Option[Map[String, String]] = Some(Map("min" -> "2", "max" -> "35"))

  "RuleRef" should {
    val expected = Map(
      "id" -> expectOk(id = ID),
      "id, errorId" -> expectOk(id = ID, errorId = ERROR_ID),
      "id, errorId, msg" -> expectOk(id = ID, errorId = ERROR_ID, errorMsg = MSG),
      "id, errorId, msg, params" -> expectOk(id = ID, errorId = ERROR_ID, errorMsg = MSG, parameters = PARAMS),
      "id, errorId, msgTemplate" -> expectOk(id = ID, errorId = ERROR_ID, errorMsgTemplate = MSG_TEMPLATE),
      "id, errorId, msgTemplate, params" -> expectOk(id = ID, errorId = ERROR_ID, errorMsgTemplate = MSG_TEMPLATE, parameters = PARAMS),

      "id, msg" -> expectOk(id = ID, errorMsg = MSG),
      "id, msg, params" -> expectOk(id = ID, errorMsg = MSG, parameters = PARAMS),
      "id, msgTemplate" -> expectOk(id = ID, errorMsgTemplate = MSG_TEMPLATE),
      "id, msgTemplate, params" -> expectOk(id = ID, errorMsgTemplate = MSG_TEMPLATE, parameters = PARAMS),

      "id, params" -> expectOk(id = ID, parameters = PARAMS),
      "id, msgTemplate" -> expectOk(id = ID, errorId = ERROR_ID, errorMsgTemplate = MSG_TEMPLATE)
    ) ++ Map(
      "empty config" -> expectError(),
      "no id, with errorId" -> expectError(errorId = ERROR_ID),
      "no id, with errorId, msg" -> expectError(errorId = ERROR_ID, errorMsg = MSG),
      "no id, with errorId, msg, params" -> expectError(errorId = ERROR_ID, errorMsg = MSG, parameters = PARAMS),
      "no id, with errorId, msgTemplate" -> expectError(errorId = ERROR_ID, errorMsgTemplate = MSG_TEMPLATE),
      "no id, with errorId, msgTemplate, params" -> expectError(errorId = ERROR_ID, errorMsgTemplate = MSG_TEMPLATE, parameters = PARAMS)
    )

    expected.foreach {
      case (label, (config, n @ None)) =>
        s"fail on $label" in intercept[Exception] {
          RuleRef(config)
        }
      case (label, (config, e @ Some(expected))) =>
        s"parse on $label" in {
          RuleRef(config) == expected
        }

    }
  }

  def expectOk(id: Option[String] = None,
                  errorId: Option[String] = None,
                  errorMsg: Option[String] = None,
                  errorMsgTemplate: Option[String] = None,
                  parameters: Option[Map[String, String]] = None
                   ): (Config, Option[RuleRef]) = {

    expectedRef(true,
      id = id, errorId = errorId, errorMsg = errorMsg, errorMsgTemplate = errorMsgTemplate, parameters = parameters
    )
  }
  def expectError(id: Option[String] = None,
                  errorId: Option[String] = None,
                  errorMsg: Option[String] = None,
                  errorMsgTemplate: Option[String] = None,
                  parameters: Option[Map[String, String]] = None
                   ): (Config, Option[RuleRef]) = {

    expectedRef(false,
      id = id, errorId = errorId, errorMsg = errorMsg, errorMsgTemplate = errorMsgTemplate, parameters = parameters
    )
  }

  def expectedRef(expectedOk: Boolean,
                  id: Option[String] = None,
                  errorId: Option[String] = None,
                  errorMsg: Option[String] = None,
                  errorMsgTemplate: Option[String] = None,
                  parameters: Option[Map[String, String]]
                   ): (Config, Option[RuleRef]) = {

    /*
    """
      |    {
      |      id="length"
      |      errorId="002"
      |      errorMsgTemplate = "@{cellName} must have an entry."
              parameters {
                min=2
                max=35
              }
      |    }
    """.stripMargin
     */

    def cfg(key: String, value: Option[String]): String = value.map(key + "=\"" + _ + "\"").getOrElse("")

    val parametersStr = parameters.map{_.map{
          case (key, value) => cfg(key, value)
        }
      }
      .map{_.mkString("\n")}
      .map{params => s"parameters {\n$params\n}"}
      .getOrElse("")
    val configStr: String = s"""
      |    {
      |      ${cfg("id", id)}
      |      ${cfg("errorId", errorId)}
      |      ${cfg("errorMsg", errorMsg)}
      |      ${cfg("errorMsgTemplate", errorMsgTemplate)}
      |      $parametersStr
      |    }
    """.stripMargin
    val config = ConfigFactory.parseReader(new StringReader(configStr))

    def leftOrRight(leftAndRight: (Option[String], Option[String])): Either[String, String] = leftAndRight match {
      case (Some(template), None) => Left(template)
      case (None, Some(msg)) => Right(msg)
      case wrong => fail(wrong.toString)
    }

    val expectedRuleDef: Option[RuleRef] =
      if (expectedOk) {
        val errorMsgEither =
          if (errorMsgTemplate != None || errorMsg != None) Some(leftOrRight(errorMsgTemplate -> errorMsg))
          else None

        Some(RuleRef(id.get, errorId, errorMsgEither, parameters))
      } else {
        None
      }

    config -> expectedRuleDef
  }

}
