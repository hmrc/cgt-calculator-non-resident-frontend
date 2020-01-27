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

package common

import uk.gov.hmrc.play.test.UnitSpec

class TransformersSpec extends UnitSpec{

  "stringToBigDecimal" should {
    "return a BigDecimal" when {
      "the transformation fails" in {
        val result = Transformers.stringToBigDecimal("1000")
        result shouldBe BigDecimal(1000)
      }
    }

    "return zero as a BigDecimal" when {
      "the transformation fails" in {
        val result = Transformers.stringToBigDecimal("FAIL")
        result shouldBe BigDecimal(0)
      }
    }
  }

  "optionalBigDecimalToString" should {
    "return an empty String" when {
      "no BigDecimal" in {
        val result = Transformers.optionalBigDecimalToString(None)
        result shouldBe ""
      }
    }

    "returna String containing a value" when {
      "given a BigDecimal" in {
        val result = Transformers.optionalBigDecimalToString(Some(BigDecimal(100.10)))
          result shouldBe "100.10"
      }
    }
  }

  "stringToInteger" should {
    "return the correct integer" when {
      "given a string only containing an integer" in {
        val result = Transformers.stringToInteger("100")
        result shouldBe 100
      }
    }

    "return an integer of zero" when {
      "given string that can not be converted to an integer" in {
        val result = Transformers.stringToInteger("100FAIL")
        result shouldBe 0
      }
    }
  }

  "finalDate" should {
    "return a key" in {
      val result = Transformers.finalDate(true)
      result shouldBe "calc.privateResidenceRelief.questionBetween.partOneAndTwo"
    }
  }

  "localDateMonthKey" should {
    "return a the correct message key for the month" in {
      Transformers.localDateMonthKey(1) shouldBe "calc.month.1"
      Transformers.localDateMonthKey(2) shouldBe "calc.month.2"
      Transformers.localDateMonthKey(3) shouldBe "calc.month.3"
      Transformers.localDateMonthKey(4) shouldBe "calc.month.4"
      Transformers.localDateMonthKey(5) shouldBe "calc.month.5"
      Transformers.localDateMonthKey(6) shouldBe "calc.month.6"
      Transformers.localDateMonthKey(7) shouldBe "calc.month.7"
      Transformers.localDateMonthKey(8) shouldBe "calc.month.8"
      Transformers.localDateMonthKey(9) shouldBe "calc.month.9"
      Transformers.localDateMonthKey(10) shouldBe "calc.month.10"
      Transformers.localDateMonthKey(11) shouldBe "calc.month.11"
      Transformers.localDateMonthKey(12) shouldBe "calc.month.12"
    }
  }
}
