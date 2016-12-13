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

package forms.nonResident

import java.time.LocalDate

import models.nonresident.DisposalDateModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.nonresident.DisposalDateForm._
import assets.MessageLookup.{NonResident => messages}

class DisposalDateFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model" should {

      lazy val acquisitionDate = LocalDate.of(2010, 3, 1)
      lazy val model = DisposalDateModel(1, 4, 2016)
      lazy val form = disposalDateForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("disposalDateDay" -> "1", "disposalDateMonth" -> "4", "disposalDateYear" -> "2016")
      }
    }

    "passing in a valid map" should {

      lazy val acquisitionDate = LocalDate.of(2010, 3, 1)
      lazy val map = Map("disposalDateDay" -> "29", "disposalDateMonth" -> "2", "disposalDateYear" -> "2016")
      lazy val form = disposalDateForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(DisposalDateModel(29, 2, 2016))
      }
    }

    "passing in a date has letters in it" should {

      lazy val map = Map("disposalDateDay" -> "a", "disposalDateMonth" -> "b", "disposalDateYear" -> "c")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.hasErrors shouldBe true
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.errors.head.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a date without a day" should {

      lazy val map = Map("disposalDateDay" -> "", "disposalDateMonth" -> "4", "disposalDateYear" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("disposalDateDay").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a date without a month" should {

      lazy val map = Map("disposalDateDay" -> "1", "disposalDateMonth" -> "", "disposalDateYear" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("disposalDateMonth").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a date without a year" should {

      lazy val map = Map("disposalDateDay" -> "1", "disposalDateMonth" -> "4", "disposalDateYear" -> "")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("disposalDateYear").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a date with a days value over 31" should {

      lazy val map = Map("disposalDateDay" -> "32", "disposalDateMonth" -> "4", "disposalDateYear" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a date with a days value less than 1" should {

      lazy val map = Map("disposalDateDay" -> "0", "disposalDateMonth" -> "4", "disposalDateYear" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a date with a months value over 12" should {

      lazy val map = Map("disposalDateDay" -> "1", "disposalDateMonth" -> "13", "disposalDateYear" -> "2009")
      lazy val form = disposalDateForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a date with a months value over 1" should {

      lazy val map = Map("disposalDateDay" -> "1", "disposalDateMonth" -> "0", "disposalDateYear" -> "2009")
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
