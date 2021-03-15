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

package uk.gov.hmrc.services.validation

import org.mvel2.MVEL
import org.mvel2.templates.TemplateRuntime

import java.text.SimpleDateFormat
import scala.annotation.tailrec
import scala.util.{Try, Success, Failure}
import scala.collection.JavaConversions._
import scala.collection.convert.Wrappers

object Utils {
  def parseTemplate(template: String, parameters: Map[String, String]): String = {

    def replaceAllEntries(template: String, replacements: Seq[(String, String)]): String = {
      template.replace(s"@{${replacements.head._1}}", replacements.head._2)
    }

    @tailrec
    def replaceUntilMapIsEmpty(template: String, parameters: Seq[(String, String)]): String = {
      if (parameters.isEmpty) template
      else replaceUntilMapIsEmpty(replaceAllEntries(template, parameters), parameters.tail)
    }

    replaceUntilMapIsEmpty(template, parameters.toSeq)
  }

//  def compileExpression(expression: String): java.io.Serializable = MVEL.compileExpression(expression)

  def compareCellToRule(regex: Option[String], isDate: Boolean, cellValue: String): Boolean = {

    if (isDate) {
      Try {
        val date = new SimpleDateFormat("yyyy-mm-dd").parse(cellValue)
        println("date is " + date)
        date
      } match {
        case Failure(_) => false
        case Success(_) => true
      }
    } else {
      cellValue.matches(regex.get)
    }
//      val varsJavaMap = mutableMapAsJavaMap[String, String](collection.mutable.Map(vars.toSeq: _*))
////      println(contextObjectOpt)
//      val test = (MVEL.executeExpression(regex, varsJavaMap)) match {
//        case Wrappers.SeqWrapper(f) => Some(f.toList)
//        case f: org.mvel2.util.FastList[_] => Some(f.toArray.toList)
//        case a: Any => Some(a)
//      }
  }


  def compareCellsToGroupRule(flagValue: String, cellToCheck: String, dependentCellValue: String): Boolean = {
    if (cellToCheck == flagValue) dependentCellValue.nonEmpty else true
  }

  def mandatoryCheck(isMandatory: Boolean, cell: Cell): Boolean = {
    if (isMandatory) cell.value.isEmpty else false
  }
}
