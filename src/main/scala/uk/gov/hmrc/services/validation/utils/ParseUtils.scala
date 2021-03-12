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

import com.typesafe.config.Config
import uk.gov.hmrc.services.validation.models.GroupRuleFlags

import scala.collection.JavaConverters._
import scala.util.Try

object ParseUtils {

  def getStringOpt(config: Config, path: String): Option[String] =
    if (config.hasPath(path)) Some(config.getString(path)) else None

  def getStringSet(config: Config, path: String): Set[String] = config.getStringList(path).asScala.toSet

  def getTryGroupRuleFlags(config: Config, path: String): Try[GroupRuleFlags] = Try {
    val independent: String = config.getString(s"$path.independent")
    val dependent: String = config.getString(s"$path.dependent")
    GroupRuleFlags(independent, dependent)
  }

  def getConfigList(path: String, config: Config): List[Config] = config.getConfigList(path).asScala.toList

  def parseConfigList[A](path: String, config: Config)(factory: Config => A): List[A] = getConfigList(path, config).map(factory)

  def parseConfigListOpt[A](path: String, config: Config)(factory: Config => A): Option[List[A]] =
    if (config.hasPath(path)) Some(parseConfigList(path, config)(factory)) else None

  def parseConfig[A](path: String, config: Config)(factory: Config => A): A = factory(config.getConfig(path))

  def parseConfigOpt[A](path: String, config: Config)(factory: Config => A): Option[A] =
    if (config.hasPath(path)) Some(parseConfig(path, config)(factory)) else None

}
