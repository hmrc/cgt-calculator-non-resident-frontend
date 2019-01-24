/*
 * Copyright 2019 HM Revenue & Customs
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

package models

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec

class ImprovementsModelSpec extends UnitSpec {


  val testRebasedValueModel = RebasedValueModel(100)
  val beforeStartDate = DateModel(
    day = 5,
    month = 4,
    year = 2015
  )
  val afterStartDate = DateModel(
    day = 6,
    month = 4,
    year = 2015
  )

  "Post Writes" should {
    "Write data to json" when {
      "Improvements model has isClaimingImprovements = 'yes', improvementAmt and improvementAfterAmt defined" in {
        val value = ImprovementsModel("Yes", Some(100), Some(100))

        ImprovementsModel.postWrites(Some(testRebasedValueModel), beforeStartDate).writes(value) shouldBe
          Json.obj(("improvements", 100), ("improvementsAfterTaxStarted", 100))
      }
      "Improvements model has isClaimingImprovements = 'yes' and improvementAfterAmt defined" in {
        val value = ImprovementsModel("Yes", None, Some(100))

        ImprovementsModel.postWrites(Some(testRebasedValueModel), beforeStartDate).writes(value) shouldBe
          Json.obj(("improvementsAfterTaxStarted", 100))
      }
      "Improvements model has isClaimingImprovements = 'yes' and improvementAmt defined" in {
        val value = ImprovementsModel("Yes", Some(100), None)

        ImprovementsModel.postWrites(Some(testRebasedValueModel), beforeStartDate).writes(value) shouldBe
          Json.obj(("improvements", 100))
      }
      "Improvements model has isClaimingImprovements = 'yes', improvementAmt and improvementAfterAmt defined and the tax date is after 5/4/2015" in {
        val value = ImprovementsModel("Yes", Some(100), Some(100))

        ImprovementsModel.postWrites(Some(testRebasedValueModel), afterStartDate).writes(value) shouldBe
          Json.obj(("improvements", 100))
      }
    }
    "Not write data to json" when {
      "Improvements model has isClaimingImprovements = 'Yes' and improvementAfterAmt defined and the tax date is after 5/4/2015" in {
        val value = ImprovementsModel("Yes", None, Some(100))

        ImprovementsModel.postWrites(Some(testRebasedValueModel), afterStartDate).writes(value) shouldBe
          Json.obj()
      }
      "Improvements model has isClaimingImprovements = 'No', improvementAmt and improvementAfterAmt defined" in {
        val value = ImprovementsModel("No", Some(100), Some(100))

        ImprovementsModel.postWrites(Some(testRebasedValueModel), beforeStartDate).writes(value) shouldBe
          Json.obj()
      }
    }
  }
}