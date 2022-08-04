/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.services.validation.utils.ParseUtils._
import uk.gov.hmrc.services.validation.models.{CellDefinition, GroupRule}


class ValidationConfig(validationConfig: Config) {

  val cells: List[CellDefinition] = parseConfigList("fieldInfo", validationConfig){CellDefinition(_)}
  val cellsByColumn: Map[String, CellDefinition] = cells.map(c => c.column -> c).toMap
  val groupRules: Option[List[GroupRule]] = parseConfigListOpt("group-rules", validationConfig){GroupRule(_)}
}
