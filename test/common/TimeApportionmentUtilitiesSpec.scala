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

package common

class TimeApportionmentUtilitiesSpec extends CommonPlaySpec {

  "Calling .percentageOfTotalGain" should {

    "when called with 100 and 20" in {
      TimeApportionmentUtilities.percentageOfTotalGain(100, 20) shouldBe 20
    }

    "when called with 99 and 66" in {
      TimeApportionmentUtilities.percentageOfTotalGain(99, 66) shouldBe 67
    }

    "when called with 100 and 101 throw an exception" in {
      intercept[Exception] {
        TimeApportionmentUtilities.percentageOfTotalGain(100, 101)
      }
    }

    "when called with 100 and 100" in {
      TimeApportionmentUtilities.percentageOfTotalGain(100, 100) shouldBe 100
    }

    "when called with -100 and 100 throw an exception" in {
      intercept[Exception] {
        TimeApportionmentUtilities.percentageOfTotalGain(-100, 100) shouldBe 0
      }
    }

    "when called with 100 and -100 throw an exception" in {
      intercept[Exception] {
        TimeApportionmentUtilities.percentageOfTotalGain(100, -100) shouldBe 0
      }
    }
  }
}
