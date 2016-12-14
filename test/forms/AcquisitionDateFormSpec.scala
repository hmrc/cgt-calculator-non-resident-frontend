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

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{NonResident => messages}
import forms.AcquisitionDateForm
import models.AcquisitionDateModel

class AcquisitionDateFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model" should {
      val model = AcquisitionDateModel("No", None, None, None)
      lazy val form = acquisitionDateForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("hasAcquisitionDate" -> "No")
      }
    }

    "passing in a valid map with a date" should {
      val map = Map(
        "hasAcquisitionDate" -> "Yes",
        "acquisitionDateDay" -> "1",
        "acquisitionDateMonth" -> "5",
        "acquisitionDateYear" -> "2015")
      lazy val form = acquisitionDateForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionDateModel("Yes", Some(1), Some(5), Some(2015)))
      }
    }

    "passing in an invalid map with an invalid date" should {
      val map = Map(
        "hasAcquisitionDate" -> "Yes",
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

    "passing in an invalid map with missing date data" should {
      val map = Map(
        "hasAcquisitionDate" -> "Yes",
        "acquisitionDateDay" -> "1",
        "acquisitionDateYear" -> "2015")
      lazy val form = acquisitionDateForm.bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorInvalidDate}" in {
        form.error("").get.message shouldBe messages.errorInvalidDate
      }
    }

    "passing in a valid map with no date" should {
      val map = Map("hasAcquisitionDate" -> "No")
      lazy val form = acquisitionDateForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionDateModel("No", None, None, None))
      }
    }

    "passing in a valid map with an invalid date" should {
      val map = Map(
        "hasAcquisitionDate" -> "No",
        "acquisitionDateDay" -> "100",
        "acquisitionDateMonth" -> "5",
        "acquisitionDateYear" -> "2015")
      lazy val form = acquisitionDateForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionDateModel("No", Some(100), Some(5), Some(2015)))
      }
    }

    "passing in an invalid map with no answer to hasAcquisitionDate" should {
      val map = Map[String, String]("hasAcquisitionDate" -> "")
      lazy val form = acquisitionDateForm.bind(map)

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRequired}" in {
        form.error("hasAcquisitionDate").get.message shouldBe messages.errorRequired
      }
    }

    "passing in an invalid map with an incorrect answer to hasAcquisitionDate" should {
      val map = Map[String, String]("hasAcquisitionDate" -> "Maybe")
      lazy val form = acquisitionDateForm.bind(map)

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRequired}" in {
        form.error("hasAcquisitionDate").get.message shouldBe messages.errorRequired
      }
    }
  }
}
