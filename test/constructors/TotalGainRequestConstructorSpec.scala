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

package constructors

import common.CommonPlaySpec
import models._
import util.Util.trim

class TotalGainRequestConstructorSpec extends CommonPlaySpec {

  "Calling .totalGainQuery" should {

    "produce a valid query string for a flat calculation" in {
      val model = TotalGainAnswersModel(DateModel(5, 10, 2016),
        SoldOrGivenAwayModel(true),
        Some(SoldForLessModel(false)),
        DisposalValueModel(1000),
        DisposalCostsModel(100),
        Some(HowBecameOwnerModel("Gifted")),
        Some(BoughtForLessModel(false)),
        AcquisitionValueModel(2000),
        AcquisitionCostsModel(200),
        DateModel(6, 4, 2015),
        None,
        None,
        IsClaimingImprovementsModel(true),
        Some(ImprovementsModel(10, None)),
        None)

      val result = trim(TotalGainRequestConstructor.totalGainQuery(model))

      result should contain ("disposalValue" -> "1000")
      result should contain ("disposalCosts" -> "100")
      result should contain ("acquisitionValue" -> "2000")
      result should contain ("acquisitionCosts" -> "200")
      result should contain ("improvements" -> "10")
      result should contain ("disposalDate" -> "2016-10-5")
      result should contain ("acquisitionDate" -> "2015-4-6")
    }

    "produce a valid query string for multiple types calculation" in {
      val model = TotalGainAnswersModel(DateModel(5, 10, 2016),
        SoldOrGivenAwayModel(true),
        Some(SoldForLessModel(false)),
        DisposalValueModel(1000),
        DisposalCostsModel(100),
        Some(HowBecameOwnerModel("Gifted")),
        Some(BoughtForLessModel(false)),
        AcquisitionValueModel(2000),
        AcquisitionCostsModel(200),
        DateModel(4, 10, 2013),
        Some(RebasedValueModel(3000)),
        Some(RebasedCostsModel("Yes", Some(300))),
        IsClaimingImprovementsModel(true),
        Some(ImprovementsModel(10, Some(20))),
        None)

      val result = trim(TotalGainRequestConstructor.totalGainQuery(model))

      // should contain assertions are here for better errors
      result should contain ("disposalValue" -> "1000")
      result should contain ("disposalCosts" -> "100")
      result should contain ("acquisitionValue" -> "2000")
      result should contain ("acquisitionCosts" -> "200")
      result should contain ("improvements" -> "10")
      result should contain ("rebasedValue" -> "3000")
      result should contain ("rebasedCosts" -> "300")
      result should contain ("improvementsAfterTaxStarted" -> "20")
      result should contain ("disposalDate" -> "2016-10-5")
      result should contain ("acquisitionDate" -> "2013-10-4")
      result should contain theSameElementsAs Map(
        "disposalValue" -> "1000",
        "disposalCosts" ->"100",
        "acquisitionValue" ->"2000",
        "acquisitionCosts" ->"200",
        "improvements" ->"10",
        "rebasedValue" ->"3000",
        "rebasedCosts" ->"300",
        "improvementsAfterTaxStarted" ->"20",
        "disposalDate" ->"2016-10-5",
        "acquisitionDate" ->"2013-10-4"
      )
    }

    "calling afterLegislation" should {
      "return true" when {
        "the legislation date is after 1/4/1982" in {
          val afterDate = DateModel(
            day = 1,
            month = 4,
            year = 1982)

          TotalGainRequestConstructor.afterLegislation(afterDate) shouldBe true
        }
        "return false" when {
          "the legislation date is before 1/4/1982" in {
            val beforeDate = DateModel(
              day = 31,
              month = 3,
              year = 1982)

            TotalGainRequestConstructor.afterLegislation(beforeDate) shouldBe false
          }
        }
      }
    }
  }

  "calling includeLegislationCosts" should {
    "return true" when {
      "costsLegislation hasCost is defined and date is before 1/4/1982" in {
        val value = CostsAtLegislationStartModel("Yes", Some(100))
        val beforeDate = DateModel(
          day = 31,
          month = 3,
          year = 1982)

        TotalGainRequestConstructor.includeLegislationCosts(value, beforeDate) shouldBe true
      }
    }
    "return false" when {
      "costsLegislation hasCost is not defined and date is before 1/4/1982" in {
        val value = CostsAtLegislationStartModel("No", None)
        val beforeDate = DateModel(
          day = 31,
          month = 3,
          year = 1982)

        TotalGainRequestConstructor.includeLegislationCosts(value, beforeDate) shouldBe false
      }
      "costsLegislation hasCost is defined and date is after 1/4/1982" in {
        val value = CostsAtLegislationStartModel("Yes", Some(100))
        val afterDate = DateModel(
          day = 2,
          month = 4,
          year = 1982)

        TotalGainRequestConstructor.includeLegislationCosts(value, afterDate) shouldBe false
      }
    }
  }

  "calling includeRebasedValuesInCalculation" should {
    "return true" when {
      "RebasedValueModel is defined and date is before 5/4/2015" in {
        val value = RebasedValueModel(rebasedValueAmt = 100)
        val beforeDate = DateModel(
          day = 5,
          month = 4,
          year = 2015)

        TotalGainRequestConstructor.includeRebasedValuesInCalculation(Some(value), beforeDate) shouldBe true
      }
    }
    "return false" when {
      "RebasedValueModel is defined and date is after 5/4/2015" in {
        val value = RebasedValueModel(rebasedValueAmt = 100)
        val afterDate = DateModel(
          day = 6,
          month = 4,
          year = 2015)

        TotalGainRequestConstructor.includeRebasedValuesInCalculation(Some(value), afterDate) shouldBe false
      }
    }
  }
}
