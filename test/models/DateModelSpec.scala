/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.LocalDate

import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class DateModelSpec extends UnitSpec {

  "createDate" should {
    "create date from a valid model" in {
      val dateModel = DateModel(
        day = 27,
        month = 11,
        year = 2018)
      val date = LocalDate.of(2018, 11, 27)
      DateModel.createDate(dateModel) shouldBe Some(date)
    }

    "return an empty option from a invalid model" in {
      val dateModel = DateModel(
        day = 27,
        month = 30,
        year = 2018)
      DateModel.createDate(dateModel) shouldBe None
    }

    "should return json" in {
      val dateModel = DateModel(
        day = 27,
        month = 11,
        year = 2018)
      DateModel.postWrites.writes(dateModel) shouldBe Json.toJson("2018-11-27")
    }
  }
}
