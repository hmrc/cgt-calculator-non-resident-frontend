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

package forms

import assets.KeyLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import forms.DisposalDateForm._
import models.DateModel
import play.api.i18n.{Messages, MessagesApi}

class DisposalDateFormSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {

  val messagesApi: MessagesApi = fakeApplication.injector.instanceOf[MessagesApi]
  implicit val testMessages: Messages = messagesApi.preferred(fakeRequest)

  "Creating a form" when {

    "passing in a valid model" should {

      lazy val model = DateModel(1, 4, 2016)
      lazy val form = disposalDateForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("disposalDate.day" -> "1", "disposalDate.month" -> "4", "disposalDate.year" -> "2016")
      }
    }

    "passing in a valid map" should {

      lazy val map = Map("disposalDate.day" -> "29", "disposalDate.month" -> "2", "disposalDate.year" -> "2016")
      lazy val form = disposalDateForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(DateModel(29, 2, 2016))
      }
    }

    "passing in a date has letters in it" should {

      lazy val map = Map("disposalDate.day" -> "a", "disposalDate.month" -> "b", "disposalDate.year" -> "c")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.hasErrors shouldBe true
      }

      s"return an error message of '${messages.DisposalDate.errorInvalidDate}" in {
        form.errors.head.message shouldBe messages.DisposalDate.errorInvalidDate
      }
    }

    "passing in a date without a day" should {

      lazy val map = Map("disposalDate.day" -> "", "disposalDate.month" -> "4", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.DisposalDate.errorRequiredDay}" in {
        form.error("disposalDate.day").get.message shouldBe messages.DisposalDate.errorRequiredDay
      }
    }

    "passing in a date without a month" should {

      lazy val map = Map("disposalDate.day" -> "1", "disposalDate.month" -> "", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.DisposalDate.errorRequiredMonth}" in {
        form.error("disposalDate.month").get.message shouldBe messages.DisposalDate.errorRequiredMonth
      }
    }

    "passing in a date without a year" should {

      lazy val map = Map("disposalDate.day" -> "1", "disposalDate.month" -> "4", "disposalDate.year" -> "")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.DisposalDate.errorRequiredYear}" in {
        form.error("disposalDate.year").get.message shouldBe messages.DisposalDate.errorRequiredYear
      }
    }

    "passing in a date with a days value over 31" should {

      lazy val map = Map("disposalDate.day" -> "32", "disposalDate.month" -> "4", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.DisposalDate.errorNotRealDay}" in {
        form.error("disposalDate.day").get.message shouldBe messages.DisposalDate.errorNotRealDay
      }
    }

    "passing in a date with a days value less than 1" should {

      lazy val map = Map("disposalDate.day" -> "0", "disposalDate.month" -> "4", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.DisposalDate.errorNotRealDay}" in {
        form.error("disposalDate.day").get.message shouldBe messages.DisposalDate.errorNotRealDay
      }
    }

    "passing in a date with a months value over 12" should {

      lazy val map = Map("disposalDate.day" -> "1", "disposalDate.month" -> "13", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.DisposalDate.errorNotRealMonth}" in {
        form.error("disposalDate.month").get.message shouldBe messages.DisposalDate.errorNotRealMonth
      }
    }

    "passing in a date with a months value less than 1" should {

      lazy val map = Map("disposalDate.day" -> "1", "disposalDate.month" -> "0", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.DisposalDate.errorNotRealMonth}" in {
        form.error("disposalDate.month").get.message shouldBe messages.DisposalDate.errorNotRealMonth
      }
    }
  }
}
