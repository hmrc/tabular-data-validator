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
/*
class RuleDefTest extends WordSpec with Matchers {

  implicit def string2option(s: String): Option[String] = Some(s)

  val ID = "Some ID"
  val ERROR_ID: String = "Some error id"

  val MSG_TEMPL: String = "Some error msg template"
  val ERROR_MSG_TEMPLATE: Either[String, String] = Left(MSG_TEMPL)
  val MSG: String = "Some error msg"
  val ERROR_MSG: Either[String, String] = Right(MSG)

  val EXPR_TEMPL: String = "//Some expression template"
  val EXPR_TEMPLATE: Either[String, String] = Left(EXPR_TEMPL)
  val EXPRESSION: String = "//Some expression"
  val EXPR: Either[String, String] = Right(EXPRESSION)

  "RuleDef" should {
    val expected = Map(
      "msg, regex" -> expectOk(id = ID, errorId = ERROR_ID, errorMsg = MSG, expr = EXPRESSION),
      "msgTemplate, regex" -> expectOk(id = ID, errorId = ERROR_ID, errorMsgTemplate = MSG_TEMPL, expr = EXPRESSION),
      "msg, exprTemplate" -> expectOk(id = ID, errorId = ERROR_ID, errorMsg = MSG, exprTemplate = EXPR_TEMPL),
      "msgTemplate, exprTemplate" -> expectOk(id = ID, errorId = ERROR_ID, errorMsgTemplate = MSG_TEMPL, exprTemplate = EXPR_TEMPL)
    ) ++ Map(
      "regex with neither msg nor msgTemplate" -> expectError(id = ID, errorId = ERROR_ID, expr = EXPRESSION),
      "exprTemplate with neither msg nor msgTemplate" -> expectError(id = ID, errorId = ERROR_ID, exprTemplate = EXPR_TEMPL)
    )

    expected.foreach {
      case (label, (config, _ @ None)) =>
        s"fail on $label" in intercept[IllegalArgumentException] {
          RuleDef(config)
        }
      case (label, (config, _ @ Some(expected))) =>
        s"parse on $label" in {
          RuleDef(config) == expected
        }

    }

    "have proper booleans on templates/plains" in {

      // map values - templates booleans
      val m: Map[RuleDef, (Boolean, Boolean)] = Map(
        RuleDef(id = ID, errorId = ERROR_ID, errorMsg = ERROR_MSG, expr = EXPR) -> (false, false),
        RuleDef(id = ID, errorId = ERROR_ID, errorMsg = ERROR_MSG_TEMPLATE, expr = EXPR) -> (true, false),
        RuleDef(id = ID, errorId = ERROR_ID, errorMsg = ERROR_MSG, expr = EXPR_TEMPLATE) -> (false, true),
        RuleDef(id = ID, errorId = ERROR_ID, errorMsg = ERROR_MSG_TEMPLATE, expr = EXPR_TEMPLATE) -> (true, true)
      )

      m.foreach{ case (ruledef, (msgtempl, exprtempl)) =>
          ruledef.isTemplateErrorMsg shouldBe(msgtempl)
          ruledef.isPlainErrorMsg shouldBe(!msgtempl)

          ruledef.isTemplateExpression shouldBe(exprtempl)
          ruledef.isPlainExpression shouldBe(!exprtempl)
      }

    }
  }

  def expectOk(id: Option[String] = None,
                  errorId: Option[String] = None,
                  errorMsg: Option[String] = None,
                  errorMsgTemplate: Option[String] = None,
                  expr: Option[String] = None,
                  exprTemplate: Option[String] = None
                   ): (Config, Option[RuleDef]) = {

    expectedDef(true,
      id = id, errorId = errorId, errorMsg = errorMsg, errorMsgTemplate = errorMsgTemplate, expr = expr, exprTemplate = exprTemplate
    )
  }
  def expectError(id: Option[String] = None,
                  errorId: Option[String] = None,
                  errorMsg: Option[String] = None,
                  errorMsgTemplate: Option[String] = None,
                  expr: Option[String] = None,
                  exprTemplate: Option[String] = None
                   ): (Config, Option[RuleDef]) = {

    expectedDef(false,
      id = id, errorId = errorId, errorMsg = errorMsg, errorMsgTemplate = errorMsgTemplate, expr = expr, exprTemplate = exprTemplate
    )
  }

  def expectedDef(expectedOk: Boolean,
                  id: Option[String] = None,
                  errorId: Option[String] = None,
                  errorMsg: Option[String] = None,
                  errorMsgTemplate: Option[String] = None,
                  expr: Option[String] = None,
                  exprTemplate: Option[String] = None
                   ): (Config, Option[RuleDef]) = {

    /*
    """
      |    {
      |      id="MANDATORY"
      |      errorId="002"
      |      errorMsgTemplate = "@{cellName} must have an entry."
      |      regex="!(data == null || data.trim().isEmpty())"
      |    }
    """.stripMargin
     */

    def cfg(key: String, value: Option[String]): String = value.map(key + "=\"" + _ + "\"").getOrElse("")
    val configStr: String = s"""
      |    {
      |      ${cfg("id", id)}
      |      ${cfg("errorId", errorId)}
      |      ${cfg("errorMsg", errorMsg)}
      |      ${cfg("errorMsgTemplate", errorMsgTemplate)}
      |      ${cfg("regex", expr)}
      |      ${cfg("exprTemplate", exprTemplate)}
      |    }
    """.stripMargin
    val config = ConfigFactory.parseReader(new StringReader(configStr))

    def leftOrRight(leftAndRight: Tuple2[Option[String], Option[String]]): Either[String, String] = leftAndRight match {
      case (Some(template), None) => Left(template)
      case (None, Some(msg)) => Right(msg)
      case wrong => fail(wrong.toString)
    }

    val expectedRuleDef: Option[RuleDef] =
      if (expectedOk) {
        val errorMsgEither = leftOrRight(errorMsgTemplate -> errorMsg)
        val exprEither = leftOrRight(exprTemplate -> expr)

        Some(RuleDef(id.get, errorId.get, errorMsgEither, exprEither))
      } else {
        None
      }

    config -> expectedRuleDef
  }

}


 */