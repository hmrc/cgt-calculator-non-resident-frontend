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

class PrivateResidenceReliefRequestConstructorSpec extends CommonPlaySpec{

  val modelDatesWithin18Months = TotalGainAnswersModel(
    DateModel(7, 7, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(7, 1, 2015),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    None
  )

  val modelDatesAcquisitionDateAfterStart = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(10, 10, 2015),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    None
  )

  val modelWithValidDates = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(10, 10, 2000),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    None
  )

  val modelWithRebasedValue = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(10, 10, 2000),
    Some(RebasedValueModel(1000)),
    None,
    IsClaimingImprovementsModel(false),
    None,
    None
  )
  val propertyLivedInModel = PropertyLivedInModel(true)

  "Calling the privateResidenceReliefQuery method" should {

    "return a string made up of the smaller substring methods" in {
      val privateResidenceReliefModel = PrivateResidenceReliefModel("Yes", Some(4), Some(5))
      val result = PrivateResidenceReliefRequestConstructor.privateResidenceReliefQuery(modelWithValidDates,
        Some(privateResidenceReliefModel),
        Some(propertyLivedInModel))

      result shouldBe PrivateResidenceReliefRequestConstructor.eligibleForPrivateResidenceRelief(Some(privateResidenceReliefModel)) +
      PrivateResidenceReliefRequestConstructor.daysClaimed(modelWithValidDates, Some(privateResidenceReliefModel)) +
      PrivateResidenceReliefRequestConstructor.daysClaimedAfter(modelWithValidDates, Some(privateResidenceReliefModel))
    }
  }

  "Calling the eligibleForPrivateResidenceRelief method" should {

    "return a true" in {
      val privateResidenceReliefModel = PrivateResidenceReliefModel("Yes", Some(4), Some(1))
      val result = PrivateResidenceReliefRequestConstructor.eligibleForPrivateResidenceRelief(Some(privateResidenceReliefModel))

      result shouldBe "&claimingPRR=true"
    }

    "return a false" in {
      val privateResidenceReliefModel = PrivateResidenceReliefModel("No", None, None)
      val result = PrivateResidenceReliefRequestConstructor.eligibleForPrivateResidenceRelief(Some(privateResidenceReliefModel))

      result shouldBe "&claimingPRR=false"
    }
  }

  "Calling the daysClaimed method" should {

    "return a blank string when acquisition and disposal date are within 18 months of each other" in {
      val privateResidenceReliefModel = PrivateResidenceReliefModel("Yes", Some(4), Some(5))
      val result = PrivateResidenceReliefRequestConstructor.daysClaimed(modelDatesWithin18Months, Some(privateResidenceReliefModel))

      result shouldBe ""
    }

    "return a blank string with prr not claimed" in {
      val privateResidenceReliefModel = PrivateResidenceReliefModel("No", Some(4), Some(5))
      val result = PrivateResidenceReliefRequestConstructor.daysClaimed(modelWithValidDates, Some(privateResidenceReliefModel))

      result shouldBe ""
    }

    "return a valid string that is the sum of the two fields when prr is claimed" in {
      val privateResidenceReliefModel = PrivateResidenceReliefModel("Yes", Some(4), Some(5))
      val result = PrivateResidenceReliefRequestConstructor.daysClaimed(modelWithValidDates, Some(privateResidenceReliefModel))

      result shouldBe "&daysClaimed=9"
    }
  }

  "Calling the daysClaimedAfter method" should {

    "return a blank string when acquisition and disposal date are within 18 months of each other" in {
      val privateResidenceReliefModel = PrivateResidenceReliefModel("Yes", Some(4), Some(5))
      val result = PrivateResidenceReliefRequestConstructor.daysClaimedAfter(modelDatesWithin18Months, Some(privateResidenceReliefModel))

      result shouldBe ""
    }

    "return a blank string when acquisition date is after tax start date" in {
      val privateResidenceReliefModel = PrivateResidenceReliefModel("Yes", Some(4), Some(5))
      val result = PrivateResidenceReliefRequestConstructor.daysClaimedAfter(modelDatesAcquisitionDateAfterStart, Some(privateResidenceReliefModel))

      result shouldBe ""
    }

    "return a value for days claimed after with a valid acquisition date" in {
      val privateResidenceReliefModel = PrivateResidenceReliefModel("Yes", Some(4), Some(5))
      val result = PrivateResidenceReliefRequestConstructor.daysClaimedAfter(modelWithValidDates, Some(privateResidenceReliefModel))

      result shouldBe "&daysClaimedAfter=5"
    }

    "return a value for days claimed after with a rebased value" in {
      val privateResidenceReliefModel = PrivateResidenceReliefModel("Yes", Some(4), Some(5))
      val result = PrivateResidenceReliefRequestConstructor.daysClaimedAfter(modelWithRebasedValue, Some(privateResidenceReliefModel))

      result shouldBe "&daysClaimedAfter=5"
    }
  }

  "Calling the checkLivedInProperty method" when {

    "supplied with None" should {
      val result = PrivateResidenceReliefRequestConstructor.checkLivedInProperty(None)

      "return false" in {
        result shouldEqual false
      }
    }

    "property was lived in" should {
      val result = PrivateResidenceReliefRequestConstructor.checkLivedInProperty(Some(propertyLivedInModel))

      "return true" in {
        result shouldEqual true
      }
    }

    "property was not lived in" should {
      val result = PrivateResidenceReliefRequestConstructor.checkLivedInProperty(Some(PropertyLivedInModel(false)))

      "return false" in {
        result shouldEqual false
      }
    }
  }
}
