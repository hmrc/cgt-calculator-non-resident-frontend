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

package constructors

import models._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class YourAnswersConstructorSpec extends UnitSpec with WithFakeApplication {

  "Calling .fetchYourAnswers" when {

    "only fetching total gain answers" should {
      val model = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
        SoldOrGivenAwayModel(true),
        Some(SoldForLessModel(false)),
        DisposalValueModel(1000),
        DisposalCostsModel(100),
        Some(HowBecameOwnerModel("Gifted")),
        Some(BoughtForLessModel(false)),
        AcquisitionValueModel(2000),
        AcquisitionCostsModel(200),
        AcquisitionDateModel("Yes", Some(4), Some(10), Some(2013)),
        Some(RebasedValueModel(Some(3000))),
        Some(RebasedCostsModel("Yes", Some(300))),
        ImprovementsModel("Yes", Some(10), Some(20)),
        Some(OtherReliefsModel(30)))
      lazy val result = YourAnswersConstructor.fetchYourAnswers(model)

      "contain the answers from sale details" in {
        lazy val salesDetails = SalesDetailsConstructor.salesDetailsRows(model)

        result.containsSlice(salesDetails) shouldBe true
      }

      "contain the answers from purchase details" in {
        lazy val purchaseDetails = PurchaseDetailsConstructor.getPurchaseDetailsSection(model)

        result.containsSlice(purchaseDetails) shouldBe true
      }

      "contain the answers from property details" in {
        lazy val propertyDetails = PropertyDetailsConstructor.propertyDetailsRows(model)

        result.containsSlice(propertyDetails) shouldBe true
      }

      "contain the answers from deduction details" in {
        lazy val deductionDetails = DeductionDetailsConstructor.deductionDetailsRows(model)

        result.containsSlice(deductionDetails) shouldBe true
      }
    }
  }
}
