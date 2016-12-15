/*
 * Copyright 2016 HM Revenue & Customs
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

class TaxDatesSpec extends UnitSpec {

  "Calling dateAfterStart method" should {

    "return a true if date entered is after the 5th April 2015" in {
      TaxDates.dateAfterStart(6, 4, 2015) shouldBe true
    }

    "return a false if date entered is the 5th April 2015" in {
      TaxDates.dateAfterStart(5, 4, 2015) shouldBe false
    }

    "return a false if date entered is before the 5th April 2015" in {
      TaxDates.dateAfterStart(4, 4, 2015) shouldBe false
    }
  }

  "Calling taxYearStringToInteger" should {
    "return 2016 from 2015/16 tax year" in {
      TaxDates.taxYearStringToInteger("2015/16") shouldBe 2016
    }

    "return 2017 from 2016/17 tax year" in {
      TaxDates.taxYearStringToInteger("2016/17") shouldBe 2017
    }
  }
}
