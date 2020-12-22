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

package uk.gov.hmrc.services.validation

import org.mvel2.MVEL
import org.mvel2.templates.TemplateRuntime

import scala.collection.JavaConversions._
import scala.collection.convert.Wrappers

object Utils {
  def parseTemplate(template: String, parameters: Map[String, String]): String =
    TemplateRuntime.eval(template, mapAsJavaMap(parameters)) match {
      case s: String => s
    }

  def compileExpression(expression: String, baseScript: Option[String]): java.io.Serializable = {
    val exprToCompile = baseScript.map(_ + "\n" + expression).getOrElse(expression)
     compileExpression(exprToCompile)
  }

  def compileExpression(expression: String): java.io.Serializable = MVEL.compileExpression(expression)

  def executeExpression(compiledExp: java.io.Serializable, contextObjectOpt: Option[AnyRef], vars: Map[String, Any]): Option[Any] = {

      val varsJavaMap = mutableMapAsJavaMap[String, Any](collection.mutable.Map(vars.toSeq: _*))
//      println(contextObjectOpt)
      contextObjectOpt.map{ ctx => MVEL.executeExpression(compiledExp, ctx, varsJavaMap)}
      .getOrElse(MVEL.executeExpression(compiledExp, varsJavaMap)) match {
        case Wrappers.SeqWrapper(f) => Some(f.toList)
        case f: org.mvel2.util.FastList[_] => Some(f.toArray.toList)
        case a: Any => Some(a)
      }

  }
}
