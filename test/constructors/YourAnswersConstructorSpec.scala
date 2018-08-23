/*
 * Copyright 2018 HM Revenue & Customs
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

import models._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class YourAnswersConstructorSpec extends UnitSpec with WithFakeApplication {

  val totalGainModel = TotalGainAnswersModel(DateModel(5, 10, 2016),
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
    ImprovementsModel("Yes", Some(10), Some(20)),
    Some(OtherReliefsModel(30)))

  "Calling .fetchYourAnswers" when {

    "only fetching total gain answers" should {

      lazy val result = YourAnswersConstructor.fetchYourAnswers(totalGainModel)


      "contain the answers from sale details" in {
        lazy val salesDetails = SalesDetailsConstructor.salesDetailsRows(totalGainModel)

        result.containsSlice(salesDetails) shouldBe true
      }

      "contain the answers from purchase details" in {
        lazy val purchaseDetails = PurchaseDetailsConstructor.getPurchaseDetailsSection(totalGainModel)

        result.containsSlice(purchaseDetails) shouldBe true
      }

      "contain the answers from property details" in {
        lazy val propertyDetails = PropertyDetailsConstructor.propertyDetailsRows(totalGainModel)

        result.containsSlice(propertyDetails) shouldBe true
      }

      "contain the answers from deduction details" in {
        lazy val deductionDetails = DeductionDetailsConstructor.deductionDetailsRows(totalGainModel, livedIn = Some(PropertyLivedInModel(false)))

        result.containsSlice(deductionDetails) shouldBe false
      }
    }
  }

  "fetching when supplied with a propertyLivedInModel and a PRR model" when {

    lazy val propertyLivedInRow = DeductionDetailsConstructor.propertyLivedInQuestionRow(Some(PropertyLivedInModel(true)))

    "the property has been lived in" should {
      val prrModel = PrivateResidenceReliefModel("Yes", Some(1))
      val propertyLivedInModel = PropertyLivedInModel(true)
      lazy val result = YourAnswersConstructor.fetchYourAnswers(totalGainModel, Some(prrModel), None, Some(propertyLivedInModel))

      "have the propertyLivedIn question row" in {
        result.containsSlice(propertyLivedInRow) shouldBe true
      }

      "contain the answers from PRR" in {
        val deductionsSlice = DeductionDetailsConstructor.deductionDetailsRows(totalGainModel, Some(prrModel), Some(PropertyLivedInModel(true)))
        result.containsSlice(deductionsSlice) shouldBe true
      }
    }

    "the property hasn't been lived in" should {
      val prrModel = PrivateResidenceReliefModel("Irrelevant string", Some(1))
      val propertyLivedInModel = PropertyLivedInModel(false)
      lazy val result = YourAnswersConstructor.fetchYourAnswers(totalGainModel, Some(prrModel), None, Some(propertyLivedInModel))

      "not have the propertyLivedIn question row" in {
        result.containsSlice(propertyLivedInRow) shouldBe false
      }


      "not contain the answers from PRR" in {
        val deductionsSlice = DeductionDetailsConstructor.deductionDetailsRows(totalGainModel, Some(prrModel), Some(PropertyLivedInModel(true)))
        result.containsSlice(deductionsSlice) shouldBe false
      }
    }
  }
}
