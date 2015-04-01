/*
 * Copyright 2015 HM Revenue & Customs
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

package uk.gov.hmrc.helpers

/**
 * Created by User 12 on 14/01/2015.
 */
class ValidationContext {


  val dateFormat =   ("(^(((0[1-9]|[12][0-9])[\\/](0[1-9]|1[012]))|((29|30|31)[\\/](0[13578]|1[02]))|((29|30)[\\/](0[4,6,9]|11)))[\\/](10|11|12|13|14|15|16|17|18|19|[2-9][0-9])\\d\\d$)|(^29[\\/]02[\\/](10|11|12|13|14|15|16|17|18|19|[2-9][0-9])(00|04|08|12|16|20|24|28|32|36|40|44|48|52|56|60|64|68|72|76|80|84|88|92|96)$)")

  val dateFormatter = new java.text.SimpleDateFormat("dd/MM/yyyy") //expensive object to create and not thread safe... containes Calandar which is expensive and not thread safe


  def getMinDate = dateFormatter.parse("06/04/2014")
  def getMaxDate = dateFormatter.parse("05/07/2014")

  def notEmpty(data: String): Boolean = {
    !(data == null || data.trim.isEmpty)
  }

  def length(min: Int, max: Int, data: String): Boolean = {
    data.length >= min && data.length <= max
  }

  def characters(firstString: String, remainingString: String, data: String): Boolean = {
    if (data.length >= 1) {
      var rx1 = data.trim.substring(0, 1).matches(firstString)
      var rx2 = data.substring(1).matches(remainingString)

      rx1 && rx2
    } else false
  }

  def dateFormat(data:String): Boolean = {
    data.trim.matches(dateFormat)
  }

  def isPast(data: String, currDate: java.util.Date): Boolean = {
    if (data.trim.matches(dateFormat)) {
      var date1 = dateFormatter.parse(data)
      date1.before(currDate)
    } else true
  }

  def isDateValid(data: String): Boolean = {
    if (data.trim.matches(dateFormat)) {
      var birthYear = Integer.parseInt(data.trim.substring(6))
      var currentYear = java.util.Calendar.getInstance.get(java.util.Calendar.YEAR)
      currentYear - birthYear <= 130
    } else true
  }

  def mandatoryM(dataL: String, dataM: String): Boolean = {
    if (dataL.equals("A") || dataL.equals("B") || dataL.equals("C")) notEmpty(dataM)
    else true
  }

  def mandatoryP(dataL: String, dataP: String): Boolean = {
    if (dataL.equals("A") || dataL.equals("B") || dataL.equals("C") || dataL.equals("D") || dataL.equals("E")) notEmpty(dataP)
    else true
  }

  def mandatoryQ(dataL: String, dataQ: String): Boolean = {
    if (dataL.equals("A") || dataL.equals("B") || dataL.equals("C") || dataL.equals("D") || dataL.equals("E")) notEmpty(dataQ)
    else true
  }

  def mandatoryR(dataL: String, dataR: String): Boolean = {
    if (dataL.equals("A") || dataL.equals("B") || dataL.equals("C") || dataL.equals("D") || dataL.equals("E")) notEmpty(dataR)
    else true
  }

  def mandatoryS(dataL: String, dataS: String): Boolean = {
    if (dataL.equals("A") || dataL.equals("B") || dataL.equals("C") || dataL.equals("D") || dataL.equals("E")) notEmpty(dataS)
    else true
  }

  def mandatoryT(dataL: String, dataT: String): Boolean = {
    if (dataL.equals("A") || dataL.equals("B") || dataL.equals("C") || dataL.equals("D") || dataL.equals("E")) notEmpty(dataT)
    else true
  }

  def mandatoryU(dataL: String, dataU: String): Boolean = {
    if (dataL.equals("A") || dataL.equals("B") || dataL.equals("C") || dataL.equals("D") || dataL.equals("E")) notEmpty(dataU)
    else true
  }


  def mandatoryY(dataN: String, dataY: String): Boolean = {
    if (dataN.equals("D")) notEmpty(dataY)
    else true
  }

  def valEndDate(dataN: String, dataO: String): Boolean = {

    if (dataN.trim.matches(dateFormat) && dataO.trim.matches(dateFormat)) {
      var startDate = dateFormatter.parse(dataN)
      var endDate = dateFormatter.parse(dataO)

      startDate.before(endDate) || startDate.equals(endDate)

    } else true
  }

  def reportPeriod(dataN: String, dataO: String): Boolean = {

    if (dataN.trim.matches(dateFormat) && dataO.trim.matches(dateFormat)) {
      var startDate = dateFormatter.parse(dataN)
      var endDate = dateFormatter.parse(dataO)
      var curr = new java.util.Date
      if ((endDate.before(curr)) && (endDate.after(startDate) || endDate.equals(startDate))) {

        endDate.after(getMinDate) && endDate.before(getMaxDate)
      } else true
    } else true
  }

  def checkAmount(data: String): Boolean = {
    if (data.trim.matches("^\\d*.{1}\\d{2}$")) {
      data.trim.matches("^[0-9.]{1,12}$")
    } else true
  }

  def mandatoryD(dataD: String, dataF: String): Boolean = {
    (!dataD.isEmpty && dataF.isEmpty || dataD.isEmpty && !dataF.isEmpty || !dataD.isEmpty && !dataF.isEmpty)
  }

  def mandatoryE(dataE: String, dataF: String): Boolean = {
    (!dataE.isEmpty && dataF.isEmpty || dataE.isEmpty && !dataF.isEmpty || !dataE.isEmpty && !dataF.isEmpty)
  }

}
