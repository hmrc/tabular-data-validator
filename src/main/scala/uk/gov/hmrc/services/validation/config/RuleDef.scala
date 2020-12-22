/*
 * Copyright 2020 HM Revenue & Customs
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

import ParseUtils._
import com.typesafe.config.Config
import uk.gov.hmrc.services.validation.Utils

/**
 * This is a definition, template of the rule.
 * From that definition the actual rule should be created.
 *
 * @param id
 * @param errorId
 * @param errorMsg
 * @param expr
 */
case class RuleDef(id: String, errorId: String, errorMsg: Either[String, String], expr: Either[String, String]) {
  require(!id.isEmpty)
  require(!errorId.isEmpty)


  def isTemplateErrorMsg: Boolean = errorMsg.isLeft
  def isPlainErrorMsg: Boolean = errorMsg.isRight

  def isTemplateExpression: Boolean = expr.isLeft
  def isPlainExpression: Boolean = expr.isRight

}

object RuleDef {
  val RULE_ID = "id"
  val ERROR_ID = "errorId"
  val ERROR_MSG = "errorMsg"
  val ERROR_MSG_TEMPLATE = "errorMsgTemplate"
  val EXPR = "expr"
  val EXPR_TEMPLATE = "exprTemplate"

  def apply(ruleConfig: Config): RuleDef = {
    val id = ruleConfig.getString(RULE_ID)
    val errorId = ruleConfig.getString(ERROR_ID)

    val errorMsgEither = eitherConfig(Left(ERROR_MSG_TEMPLATE), Right(ERROR_MSG), ruleConfig)
    val exprEither = eitherConfig(Left(EXPR_TEMPLATE), Right(EXPR), ruleConfig)

    RuleDef(id = id,
      errorId = errorId,
      errorMsg = errorMsgEither,
      expr = exprEither)
  }

}

  //////////////////////////////////////////////
/**
 * Reference (by id) to an existing RuleDef.
 * Optionally customisable items are: errorId, errorMsg
 * If expression template in referenced RuleDef has parameters, they should be customised here with parameters map.
 *
 * @param id
 * @param errorId
 * @param errorMsg
 * @param parameters
 */
case class RuleRef(id: String,
                   errorId: Option[String] = None,
                   errorMsg: Option[Either[String, String]] = None,
                   parameters: Option[Map[String, String]] = None)

object RuleRef {
  val MANDATORY_RULE_REF: RuleRef = RuleRef(id = "MANDATORY")

  val RULE_PARAMETERES = "parameters"

  def apply(refConfig: Config): RuleRef = {
    implicit val implicitConfig = refConfig
    val id = refConfig.getString(RuleDef.RULE_ID)
    val errorId = getStringOpt(RuleDef.ERROR_ID)
    val errorMsgEither = eitherConfigOpt(Left(RuleDef.ERROR_MSG_TEMPLATE), Right(RuleDef.ERROR_MSG))
    val errorMsg = getStringOpt(RuleDef.ERROR_MSG)

    val parameters: Option[Map[String, String]] = parseConfigOpt(RULE_PARAMETERES)(asMap(_))

    RuleRef(id = id,
      errorId = errorId,
      errorMsg = errorMsgEither,
      parameters = parameters)
  }
}

  //////////////////////////////////////////////

/**
 * This is the actual rule to apply.
 * Not intended to be configured in a file, created internally instead from a given RuleDef.
 *
 * @param id
 * @param errorId
 * @param errorMsg
 * @param expr
 */
case class Rule(id: String,
                errorId: String,
                errorMsg: Either[String, String],
                parameters: Option[Map[String, String]],
                expr: String,
                compiledExpr: java.io.Serializable) {
  def isTemplateErrorMsg: Boolean = errorMsg.isLeft
  def isPlainErrorMsg: Boolean = errorMsg.isRight
}


object Rule {

  def apply(ruleDef: RuleDef, ruleRefOpt: Option[RuleRef], baseScript: Option[String]): Rule = {
    //todo add more requirements
    require(ruleRefOpt.map{_.id == ruleDef.id}.getOrElse(true))

    val errorId: String = ruleRefOpt.flatMap(_.errorId).getOrElse(ruleDef.errorId)
    val errorMsg: Either[String, String] = ruleRefOpt.flatMap(_.errorMsg).getOrElse(ruleDef.errorMsg)
    val parametersOpt: Option[Map[String, String]] = ruleRefOpt.flatMap(_.parameters)

    val expr: String =
      if (ruleDef.isTemplateExpression) {
        //endorse template expression
        val template = ruleDef.expr.left.get
        Utils.parseTemplate(template, parametersOpt.getOrElse(Map()))
      } else {
        ruleDef.expr.right.get
      }

    val compiledExpr = Utils.compileExpression(expr, baseScript)

    Rule(id = ruleDef.id,
      errorId = errorId,
      errorMsg = errorMsg,
      parameters = parametersOpt,
      expr = expr,
      compiledExpr = compiledExpr
    )

  }

}
