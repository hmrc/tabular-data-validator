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

/**
 * Created by user02 on 10/13/14.
 */
class RuleTest extends WordSpec with Matchers {

  val ID = "Some ID"
  val ERROR_ID: String = "Some error id"

  val MSG_TEMPL: String = "Some error msg template"
  val ERROR_MSG_TEMPLATE: Either[String, String] = Left(MSG_TEMPL)
  val MSG: String = "Some error msg"
  val ERROR_MSG: Either[String, String] = Right(MSG)

  val EXPR_TEMPL: String = """println("@{data}")"""
  val EXPR_TEMPLATE: Either[String, String] = Left(EXPR_TEMPL)
  val EXPRESSION: String = """"println(data)""""
  val EXPR: Either[String, String] = Right(EXPRESSION)

  val SCRIPT: String =
    """
      |def println(s) {
      | System.out.println(s)
      |}
    """.stripMargin
  val SOME_SCRIPT: Option[String] = Some(SCRIPT)
  val NONE_SCRIPT: Option[String] = None

  val PARAMS: Map[String, String] = Map("data" -> "Hello world.")
  val PARAMS_OPT: Option[Map[String, String]] = Some(PARAMS)

  val PARSED_EXPR_TEMPLATE: String = Utils.parseTemplate(EXPR_TEMPL, PARAMS)
  val COMPILED_EXPR_TEMPLATE = Utils.compileExpression(s"$SCRIPT\n$PARSED_EXPR_TEMPLATE")
  val COMPILED_EXPR = Utils.compileExpression(s"$SCRIPT\n$EXPRESSION")

//  println(COMPILED_EXPR.toString)

  val RULEDEF_TE = RuleDef(id = ID, errorId = ERROR_ID, errorMsg = ERROR_MSG_TEMPLATE, expr = EXPR)
  val RULEDEF_TT = RuleDef(id = ID, errorId = ERROR_ID, errorMsg = ERROR_MSG_TEMPLATE, expr = EXPR_TEMPLATE)
  val RULEDEF_MT = RuleDef(id = ID, errorId = ERROR_ID, errorMsg = ERROR_MSG, expr = EXPR_TEMPLATE)
  val RULEDEF_ME = RuleDef(id = ID, errorId = ERROR_ID, errorMsg = ERROR_MSG, expr = EXPR)


  "Rule" should {
    "be created from given RuleDef" in {
      val ruledef = RULEDEF_TE

      val rule = Rule(ruledef, ruleRefOpt = None, baseScript = SOME_SCRIPT)
      rule.id shouldBe(ruledef.id)
      rule.errorId shouldBe(ruledef.errorId)
      rule.errorMsg shouldBe(ruledef.errorMsg)
      rule.parameters shouldBe(None)
      rule.expr shouldBe(ruledef.expr.right.get)
      //todo: how to check equality of compiled expressions ???
      rule.compiledExpr.toString shouldBe(COMPILED_EXPR.toString)
    }

    "be created from given RuleDef and RuleRef" in {
      val ERROR_ID_ref: String = "Ref error id"

      val MSG_TEMPL_ref: String = "Ref error msg template"
      val ERROR_MSG_TEMPLATE_ref: Either[String, String] = Left(MSG_TEMPL_ref)
      val MSG_ref: String = "Ref error msg"
      val ERROR_MSG_ref: Either[String, String] = Right(MSG_ref)

      val ruledef = RULEDEF_TT
      val ruleref = RuleRef(id = ruledef.id, errorId = Some(ERROR_ID_ref), errorMsg = Some(ERROR_MSG_ref), parameters = PARAMS_OPT)

      val rule = Rule(ruledef, ruleRefOpt = Some(ruleref), baseScript = SOME_SCRIPT)
      rule.id shouldBe(ruleref.id)
      rule.errorId shouldBe(ruleref.errorId.get)
      rule.errorMsg shouldBe(ruleref.errorMsg.get)
      rule.parameters shouldBe(ruleref.parameters)
      rule.expr shouldBe(PARSED_EXPR_TEMPLATE)
      //todo: how to check equality of compiled expressions ???
      rule.compiledExpr.toString shouldBe(COMPILED_EXPR_TEMPLATE.toString)
    }

    "have proper booleans on templates/plains" in {

      // map values - templates booleans
      val m: Map[Rule, Boolean] = Map(
        Rule(id = ID, errorId = ERROR_ID, errorMsg = ERROR_MSG, parameters = PARAMS_OPT, expr = EXPRESSION, compiledExpr = COMPILED_EXPR) -> false,
        Rule(id = ID, errorId = ERROR_ID, errorMsg = ERROR_MSG_TEMPLATE, parameters = PARAMS_OPT, expr = EXPRESSION, compiledExpr = COMPILED_EXPR) -> true
      )

      m.foreach{ case (rule, msgtempl) =>
        rule.isTemplateErrorMsg shouldBe(msgtempl)
        rule.isPlainErrorMsg shouldBe(!msgtempl)
      }

    }

  }

}
