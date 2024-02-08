/*
 * Copyright 2024 HM Revenue & Customs
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

package util

import common.{CommonPlaySpec, TaxDates}

import java.time.LocalDate

class PrivateResidenceReliefDateSpec extends CommonPlaySpec {

  "Private Residence Relief" should {
    "needs to have correct value for extended period" in {
      val testDate = LocalDate.of(2020, 1, 1)
      val expectedDate = LocalDate.of(2018, 7, 1)
      val reliefDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(Some(testDate))
      reliefDateDetails.months shouldBe 18
      reliefDateDetails.dateDeducted.get shouldBe expectedDate
      reliefDateDetails.shortedPeriod shouldBe false
    }

    "needs to have correct value for extended period boundary" in {
      val testDate = LocalDate.of(2020, 4, 5)
      val expectedDate = LocalDate.of(2018, 10, 5)
      val reliefDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(Some(testDate))
      reliefDateDetails.months shouldBe 18
      reliefDateDetails.dateDeducted.get shouldBe expectedDate
      reliefDateDetails.shortedPeriod shouldBe false
    }

    "needs to have correct value for shorter period boundary" in {
      val testDate = LocalDate.of(2020, 4, 6)
      val expectedDate = LocalDate.of(2019, 7, 6)
      val reliefDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(Some(testDate))
      reliefDateDetails.months shouldBe 9
      reliefDateDetails.dateDeducted.get shouldBe expectedDate
      reliefDateDetails.shortedPeriod shouldBe true
    }

    "needs to have correct value for None date" in {
      val reliefDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(None)
      reliefDateDetails.months shouldBe 18
      reliefDateDetails.dateDeducted shouldBe None
      reliefDateDetails.shortedPeriod shouldBe false
    }
  }
}