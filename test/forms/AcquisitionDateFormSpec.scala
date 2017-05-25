/*
 * Copyright 2017 HM Revenue & Customs
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

import assets.MessageLookup.{NonResident => messages}
import forms.AcquisitionDateForm._
import models.AcquisitionDateModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class AcquisitionDateFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model" should {
      val model = AcquisitionDateModel(1, 1, 2015)
      lazy val form = acquisitionDateForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("acquisitionDateDay" -> "1", "acquisitionDateMonth" -> "1", "acquisitionDateYear" -> "2015")
      }
    }

    "passing in a valid map with a date" should {
      val map = Map(
        "acquisitionDateDay" -> "1",
        "acquisitionDateMonth" -> "5",
        "acquisitionDateYear" -> "2015")
      lazy val form = acquisitionDateForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionDateModel(1, 5, 2015))
      }
    }

    "passing in an invalid map with an invalid date" should {
      val map = Map(
        "acquisitionDateDay" -> "100",
        "acquisitionDateMonth" -> "5",
        "acquisitionDateYear" -> "2015")
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in an invalid map with a date containing a string" should {
      val map = Map(
        "acquisitionDateDay" -> "A",
        "acquisitionDateMonth" -> "B",
        "acquisitionDateYear" -> "C")
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one errors" in {
        form.hasErrors shouldBe true
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.errors.head.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in an invalid map with missing day data" should {
      val map = Map(
        "acquisitionDateMonth" -> "1",
        "acquisitionDateYear" -> "2015")
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("acquisitionDateDay").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in an invalid map with missing month data" should {
      val map = Map(
        "acquisitionDateDay" -> "1",
        "acquisitionDateYear" -> "2015")
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("acquisitionDateMonth").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in an invalid map with missing month year" should {
      val map = Map(
        "acquisitionDateDay" -> "1",
        "acquisitionDateMonth" -> "2")
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("acquisitionDateYear").get.message shouldBe messages.errorInvalidDate
      }
    }
  }
}
