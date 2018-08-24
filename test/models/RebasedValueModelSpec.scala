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

package models

import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class RebasedValueModelSpec extends UnitSpec {


    "Post Writes" should {
        "gets json object for rebase value when date is not after 5/4/2015" in {
            val value = RebasedValueModel(rebasedValueAmt = 100)
            val date = DateModel(
                day = 5,
                month = 4,
                year = 2015)
            RebasedValueModel.postWrites(date).writes(value) shouldBe Json.obj(("rebasedValue", 100))
        }
        "gets empty json object when date is after 5/4/2015" in {
            val value = RebasedValueModel(rebasedValueAmt = 100)
            val date = DateModel(
                day = 6,
                month = 4,
                year = 2015)
            RebasedValueModel.postWrites(date).writes(value) shouldBe Json.obj()
        }
    }
}
