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

package forms

import assets.KeyLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import forms.AcquisitionDateForm._
import models.DateModel
import play.api.i18n.{Messages, MessagesApi}

import java.time.LocalDate

class AcquisitionDateFormSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {

  val messagesApi: MessagesApi = fakeApplication.injector.instanceOf[MessagesApi]
  implicit val testMessages: Messages = messagesApi.preferred(fakeRequest)

  "Creating a form" when {

    "passing in a valid model" should {
      val model = DateModel(1, 1, 2015)
      lazy val form = acquisitionDateForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("acquisitionDate.day" -> "1", "acquisitionDate.month" -> "1", "acquisitionDate.year" -> "2015")
      }
    }

    "passing in a valid map with a date" should {
      val map = Map(
        "acquisitionDate.day" -> "1",
        "acquisitionDate.month" -> "5",
        "acquisitionDate.year" -> "2015")
      lazy val form = acquisitionDateForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(DateModel(1, 5, 2015))
      }
    }

    "passing in an invalid map with an invalid date" should {
      val map = Map(
        "acquisitionDate.day" -> "29",
        "acquisitionDate.month" -> "2",
        "acquisitionDate.year" -> "2015")
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.AcquisitionDate.errorNotRealDate}" in {
        form.error("acquisitionDate").get.message shouldBe messages.AcquisitionDate.errorNotRealDate
      }
    }

    "passing in an invalid map with a date containing a string" should {
      val map = Map(
        "acquisitionDate.day" -> "A",
        "acquisitionDate.month" -> "B",
        "acquisitionDate.year" -> "C")
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one errors" in {
        form.hasErrors shouldBe true
      }

      s"return an error message of '${messages.AcquisitionDate.errorInvalidDate}" in {
        form.errors.head.message shouldBe messages.AcquisitionDate.errorInvalidDate
      }
    }

    "passing in an invalid map with missing day data" should {
      val map = Map(
        "acquisitionDate.day" -> "",
        "acquisitionDate.month" -> "1",
        "acquisitionDate.year" -> "2015")
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.AcquisitionDate.errorRequiredDay}" in {
        form.error("acquisitionDate.day").get.message shouldBe messages.AcquisitionDate.errorRequiredDay
      }
    }

    "passing in an invalid map with missing month data" should {
      val map = Map(
        "acquisitionDate.day" -> "1",
        "acquisitionDate.month" -> "",
        "acquisitionDate.year" -> "2015")
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.AcquisitionDate.errorRequiredMonth}" in {
        form.error("acquisitionDate.month").get.message shouldBe messages.AcquisitionDate.errorRequiredMonth
      }
    }

    "passing in an invalid map with missing year data" should {
      val map = Map(
        "acquisitionDate.day" -> "1",
        "acquisitionDate.month" -> "2",
        "acquisitionDate.year" -> "")
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.AcquisitionDate.errorRequiredYear}" in {
        form.error("acquisitionDate.year").get.message shouldBe messages.AcquisitionDate.errorRequiredYear
      }
    }

    "passing in an invalid map with today's date" should {
      val date: LocalDate = LocalDate.now()
      lazy val map = Map(
        "acquisitionDate.day" -> date.getDayOfMonth.toString,
        "acquisitionDate.month" -> date.getMonthValue.toString,
        "acquisitionDate.year" -> date.getYear.toString)
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.AcquisitionDate.errorFutureDateGuidance}" in {
        form.error("acquisitionDate").get.message shouldBe messages.AcquisitionDate.errorFutureDateGuidance
      }
    }
  }
}
