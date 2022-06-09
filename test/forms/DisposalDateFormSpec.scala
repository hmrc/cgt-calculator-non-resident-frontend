/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.DisposalDateForm._
import models.DateModel

class DisposalDateFormSpec extends CommonPlaySpec with WithCommonFakeApplication {

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

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.errors.head.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a date without a day" should {

      lazy val map = Map("disposalDate.day" -> "", "disposalDate.month" -> "4", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDay}" in {
        form.error("disposalDate.day").get.message shouldBe messages.errorInvalidDay
      }
    }

    "passing in a date without a month" should {

      lazy val map = Map("disposalDate.day" -> "1", "disposalDate.month" -> "", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidMonth}" in {
        form.error("disposalDate.month").get.message shouldBe messages.errorInvalidMonth
      }
    }

    "passing in a date without a year" should {

      lazy val map = Map("disposalDate.day" -> "1", "disposalDate.month" -> "4", "disposalDate.year" -> "")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidYear}" in {
        form.error("disposalDate.year").get.message shouldBe messages.errorInvalidYear
      }
    }

    "passing in a date with a days value over 31" should {

      lazy val map = Map("disposalDate.day" -> "32", "disposalDate.month" -> "4", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a date with a days value less than 1" should {

      lazy val map = Map("disposalDate.day" -> "0", "disposalDate.month" -> "4", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a date with a months value over 12" should {

      lazy val map = Map("disposalDate.day" -> "1", "disposalDate.month" -> "13", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a date with a months value over 1" should {

      lazy val map = Map("disposalDate.day" -> "1", "disposalDate.month" -> "0", "disposalDate.year" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("").get.message shouldBe messages.errorInvalidDate
      }
    }
  }
}
