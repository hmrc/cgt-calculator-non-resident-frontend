/*
 * Copyright 2023 HM Revenue & Customs
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

class RebasedCostsModelSpec extends CommonPlaySpec {

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
    "not write any data to json" when{
      "rebasedValueModel is not defined" in {
        val value = RebasedCostsModel("Yes", Some(1000))
        RebasedCostsModel.postWrites(None, afterStartDate).writes(value) shouldBe Json.obj()
      }
      "acquisition date is after tax start date" in {
        val value = RebasedCostsModel("Yes", Some(1000))
        RebasedCostsModel.postWrites(Some(testRebasedValueModel), afterStartDate).writes(value) shouldBe Json.obj()
      }
      "rebasedValueModel is defined and acquisition date is after tax start date" when {
        "rebased costs model has hasRebasedCosts = 'No'" in {
          val value = RebasedCostsModel("No", Some(1000))
          RebasedCostsModel.postWrites(Some(testRebasedValueModel), afterStartDate).writes(value) shouldBe Json.obj()
        }
        "rebased cost model has rebasedCosts undefined" in {
          val value = RebasedCostsModel("Yes", None)
          RebasedCostsModel.postWrites(Some(testRebasedValueModel), afterStartDate).writes(value) shouldBe Json.obj()
        }
        "rebased cost model has rebasedCosts undefined and hasRebasedCosts ='No'" in {
          val value = RebasedCostsModel("No", None)
          RebasedCostsModel.postWrites(Some(testRebasedValueModel), afterStartDate).writes(value) shouldBe Json.obj()
        }
      }
    }
    "write the correct data to json" when {
      "rebased cost model has rebasedCosts defined and hasRebasedCosts ='Yes'" in {
        val value = RebasedCostsModel("Yes", Some(1000))
        RebasedCostsModel.postWrites(Some(testRebasedValueModel), beforeStartDate).writes(value) shouldBe Json.obj(("rebasedCosts", 1000))
      }
    }
  }
}
