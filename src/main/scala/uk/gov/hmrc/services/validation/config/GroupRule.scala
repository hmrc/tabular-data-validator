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

import com.typesafe.config.Config
import ParseUtils._
import uk.gov.hmrc.services.validation.Utils

case class GroupRule(id: String,
                   errorId: String,
                   columns: Set[String],
                   columnErrors: Map[String, Either[String, String]], // key - column, value - Left for template, Right for messsage
                   expr: String,
                   compiledExpr: java.io.Serializable) {
  /*
      id="mandatoryGHI"
      errorId="002"
      columns:["G", "H", "I"]
      expr="notEmpty(dataG) || notEmpty(dataH) || notEmpty(dataI)"
      columnErrors: {
        "G": {errorMsgTemplate = "'@{cellNameG}' or '@{cellNameH}' or '@{cellNameI}' must have an entry."}
        "I": {errorMsg = "Field must have an entry."}
      }
   */

  def isTemplateErrorMsgFor(column: String): Option[Boolean] = columnErrors.get(column).map{_.isLeft}
  def isPlainErrorMsgFor(column: String): Option[Boolean] = isTemplateErrorMsgFor(column).map{!_}
}

object GroupRule {

  def apply(rowConfig: Config, baseScript: Option[String]): GroupRule = {
    implicit val implicitConfig = rowConfig
    val id = rowConfig.getString(RuleDef.RULE_ID)
    val errorId = rowConfig.getString(RuleDef.ERROR_ID)

    val columns: Set[String] = getStringSet(rowConfig, "columns")
    if (columns.isEmpty) throw new IllegalArgumentException(s"Columns for row rule $id are not given.")

    val columnErrors: Map[String, Either[String, String]] = columns.map{ case column =>
      val path = s"columnErrors.$column"
      if (rowConfig.hasPath(path)) {
        val columnErrorConfig = rowConfig.getConfig(path)
        val errorMsgEither = eitherConfig(Left(RuleDef.ERROR_MSG_TEMPLATE), Right(RuleDef.ERROR_MSG), columnErrorConfig)
        Some(column -> errorMsgEither)
      } else {
        None
      }
    }.filter(_ != None).flatten.toMap

    val expr = rowConfig.getString("expr")
    val compiledExpr = Utils.compileExpression(expr, baseScript)

    GroupRule(id = id,
      errorId = errorId,
      columns = columns,
      columnErrors = columnErrors,
      expr = expr,
      compiledExpr = compiledExpr)
  }
}
