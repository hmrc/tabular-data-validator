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

package uk.gov.hmrc.helpers
import java.io.File

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models._

/*
class StressTest extends WordSpec with Matchers {

  import FileHelper._

  val dateFormat = ("(^(((0[1-9]|[12][0-8])[/](0[1-9]|1[012]))|((29|30|31)[/](0[13578]|1[02]))|((29|30)[/](0[4,6,9]|11)))[/](10|11|12|13|14|15|16|17|18|19|[2-9][0-9])dd$)|(^29[/]02[/](10|11|12|13|14|15|16|17|18|19|[2-9][0-9])(00|04|08|12|16|20|24|28|32|36|40|44|48|52|56|60|64|68|72|76|80|84|88|92|96)$)")

  val dateFormatter = new java.text.SimpleDateFormat("dd/MM/yyyy") //expensive object to create and not thread safe... contains Calendar which is expensive and not thread safe

  "validate the file" in {
    val data = readInputCsv(new File(System.getProperty("user.dir") + "/target/scala-2.12/test-classes/data/CSV_10000rows_header_nf.csv"))
    val config = loadConfig(new File(System.getProperty("user.dir") + "/src/test/resources/config/validation-fix.conf")).getConfig("validation-config")

    DataValidator(config).validateRows(data, Some(new ValidationContext), firstRowNum = 2, ignoreBlankRows = false) match {
      case Some(errors) => {
        errors.collect  {
          case error @ ValidationError(Cell("A",_,_), ruleId: String, errorId: String, errorMsg: String)  => {
          }
        }
        errors.length should equal(29116)
      }
      case _ => { fail("no errors") }
    }
  }
}

 */
