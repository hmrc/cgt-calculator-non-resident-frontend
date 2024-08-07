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

class ImprovementsModelSpec extends CommonPlaySpec {


  val testRebasedValueModel: RebasedValueModel = RebasedValueModel(100)
  val beforeStartDate: DateModel = DateModel(
    day = 5,
    month = 4,
    year = 2015
  )
  val afterStartDate: DateModel = DateModel(
    day = 6,
    month = 4,
    year = 2015
  )

  "Post Writes" should {
    "Write data to json" when {
      "Improvements model has improvementAfterAmt defined" in {
        val value = ImprovementsModel(0, Some(100))

        ImprovementsModel.postWrites(Some(testRebasedValueModel), beforeStartDate).writes(value) shouldBe
          Json.obj(("improvements", 0), ("improvementsAfterTaxStarted", 100))
      }
      "Improvements model has improvementAmt defined" in {
        val value = ImprovementsModel(100, None)

        ImprovementsModel.postWrites(Some(testRebasedValueModel), beforeStartDate).writes(value) shouldBe
          Json.obj(("improvements", 100))
      }
      "Improvements model has improvementAmt and improvementAfterAmt defined and the tax date is after 5/4/2015" in {
        val value = ImprovementsModel(100, Some(100))

        ImprovementsModel.postWrites(Some(testRebasedValueModel), afterStartDate).writes(value) shouldBe
          Json.obj(("improvements", 100))
      }
    }
    "Not write improvementsAfterAmt data to json" when {
      "Improvements model has improvementAfterAmt defined and the tax date is after 5/4/2015" in {
        val value = ImprovementsModel(0, Some(100))

        ImprovementsModel.postWrites(Some(testRebasedValueModel), afterStartDate).writes(value) shouldBe
          Json.obj(("improvements", 0))
      }
    }
  }
}