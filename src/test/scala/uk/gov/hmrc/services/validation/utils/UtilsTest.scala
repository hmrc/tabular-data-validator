/*
 * Copyright 2023 HM Revenue & Customs
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

import org.scalatest.{Matchers, WordSpec}

class UtilsTest extends WordSpec with Matchers {

  "Utils" when {
    "calling compareCellToRule" should {
      "return None if cellValue is empty" in {
        Utils.compareCellToRule(None, isDate = true, "") shouldBe None
      }
      "return Some(false) if date validation failed" in {
        Utils.compareCellToRule(None, isDate = true, "this is not a date") shouldBe Some(false)
      }
      "return Some(true) if date validation succeeded" in {
        Utils.compareCellToRule(None, isDate = true, "2010-11-11") shouldBe Some(true)
      }

      "return Some(false) if regex validation failed" in {
        Utils.compareCellToRule(Some("[0-9]*"), isDate = false, "these aren't numbers") shouldBe Some(false)
      }
      "return Some(true) if regex validation succeeded" in {
        Utils.compareCellToRule(Some("[0-9]*"), isDate = false, "123123123") shouldBe Some(true)
      }
    }
  }

}
