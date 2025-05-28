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

package models

import common.CommonPlaySpec
import play.api.libs.json.Json

class AcquisitionCostsModelSpec extends CommonPlaySpec {

  val beforeLegislationDate: DateModel = DateModel(
    day = 31,
    month = 3,
    year = 1982
  )
  val afterLegislationDate: DateModel = DateModel(
    day = 1,
    month = 4,
    year = 1982
  )

  "Post Writes" should {
    "Write data to json" when {
      "AcquisitionCostsModel has costsOfLegislationStartModel defined" in {
       val value = CostsAtLegislationStartModel("Yes", Some(100))
       val model = AcquisitionCostsModel(1000)

         AcquisitionCostsModel.postWrites(Some(value), beforeLegislationDate).writes(Some(model)) shouldBe
          Json.obj(("acquisitionCosts", 100))
      }
      "AcquisitionCostsModel has acquisitionCostsAmt defined" in {
        val value = CostsAtLegislationStartModel("No", Some(100))
        val model = AcquisitionCostsModel(1000)

        AcquisitionCostsModel.postWrites(Some(value), beforeLegislationDate).writes(Some(model)) shouldBe
          Json.obj(("acquisitionCosts", 0))
      }
      "AcquisitionCostsModel has costsLegislation = 'No' and acquisitionCostsAmt defined" in {
        val value = CostsAtLegislationStartModel("No", Some(100))
        val model = AcquisitionCostsModel(1000)

        AcquisitionCostsModel.postWrites(Some(value), afterLegislationDate).writes(Some(model)) shouldBe
          Json.obj(("acquisitionCosts", 1000))
      }

      "costsLegislation is defined and acquisitionCostModel is not defined" in {
        val value = CostsAtLegislationStartModel("Yes", Some(100))


        AcquisitionCostsModel.postWrites(Some(value), beforeLegislationDate).writes(None) shouldBe
          Json.obj(("acquisitionCosts", 100))
      }

      "costsLegislation hasCost = 'No' and acquisitionCostModel is not defined" in {
        val value = CostsAtLegislationStartModel("No", Some(100))

        AcquisitionCostsModel.postWrites(Some(value), beforeLegislationDate).writes(None) shouldBe
          Json.obj(("acquisitionCosts", 0))
      }

      "costsLegislation hasCost = 'Yes' and acquisitionCostModel is not defined and Date is after legislation date" in {
        val value = CostsAtLegislationStartModel("Yes", Some(100))

        AcquisitionCostsModel.postWrites(Some(value), afterLegislationDate).writes(None) shouldBe
          Json.obj(("acquisitionCosts", 0))
      }
      "costsLegislation not defined and acquisitionCostModel is defined and date is before legislation date" in {
        val model = AcquisitionCostsModel(1000)

        AcquisitionCostsModel.postWrites(None, beforeLegislationDate).writes(Some(model)) shouldBe
          Json.obj(("acquisitionCosts", 0))
      }
      "costsLegislation not defined and acquisitionCostModel is defined and date is after legislation date" in {
        val model = AcquisitionCostsModel(1000)

        AcquisitionCostsModel.postWrites(None, afterLegislationDate).writes(Some(model)) shouldBe
          Json.obj(("acquisitionCosts", 1000))
      }
      "costsLegislation not defined and acquisitionCostModel is not defined" in {

        AcquisitionCostsModel.postWrites(None, afterLegislationDate).writes(None) shouldBe
          Json.obj(("acquisitionCosts", 0))
      }

    }
  }
}
