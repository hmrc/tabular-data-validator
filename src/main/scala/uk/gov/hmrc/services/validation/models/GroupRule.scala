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

package uk.gov.hmrc.services.validation.models

import com.typesafe.config.Config
import uk.gov.hmrc.services.validation.utils.ParseUtils.{getStringSet, getTryGroupRuleFlags}

import scala.util.{Failure, Success}


case class GroupRule(id: String,
                   errorId: String,
                   columns: Set[String],
                   columnErrors: Map[String, String],
                   flags: GroupRuleFlags,
                   expectedValue: String
                    )

object GroupRule {

  val EXPECTED_VALUE: String = "expectedValue"
  val COLUMNS: String = "columns"
  val FLAGS: String = "flags"

  def apply(rowConfig: Config): GroupRule = {
    val id = rowConfig.getString(RuleDef.RULE_ID)
    val errorId = rowConfig.getString(RuleDef.ERROR_ID)

    val columns: Set[String] = getStringSet(rowConfig, COLUMNS)
    if (columns.isEmpty) throw new IllegalArgumentException(s"Columns for row rule $id are not given.")

    val columnErrors: Map[String, String] = columns.map { column =>
      val path = s"columnErrors.$column"
      if (rowConfig.hasPath(path)) {
        val columnErrorConfig = rowConfig.getConfig(path)
        val errorMsg = columnErrorConfig.getString(RuleDef.ERROR_MSG)
        Some(column -> errorMsg)
      } else {
        None
      }
    }.filter(_.isDefined).flatten.toMap

    val groupRulesFlags = getTryGroupRuleFlags(rowConfig, FLAGS) match {
      case Success(flags) => flags
      case Failure(exception) => throw exception
    }

    val expectedValue: String = rowConfig.getString(EXPECTED_VALUE)

    GroupRule(id = id,
      errorId = errorId,
      columns = columns,
      columnErrors = columnErrors,
      flags = groupRulesFlags,
      expectedValue = expectedValue
    )
  }
}
