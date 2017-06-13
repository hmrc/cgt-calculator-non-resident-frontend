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
import forms.CostsAtLegislationStartForm._
import models.CostsAtLegislationStartModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CostsAtLegislationStartFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model" should {
      val model = CostsAtLegislationStartModel("Yes", Some(BigDecimal(1500)))
      lazy val form = costsAtLegislationStartForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map(
          "hasCosts" -> "Yes",
          "costs" -> "1500")
      }
    }

    "passing in a valid map with 'No'" should {
      val map = Map(
        "hasCosts" -> "No",
        "costs" -> "")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(CostsAtLegislationStartModel("No", None))
      }
    }

    "passing in a valid map with 'No' and an invalid amount" should {
      val map = Map(
        "hasCosts" -> "No",
        "costs" -> "50.234")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(CostsAtLegislationStartModel("No", Some(50.234)))
      }
    }

    "passing in an invalid map with no answer to hasCosts" should {
      val map = Map(
        "hasCosts" -> "",
        "costs" -> "100")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRequired}" in {
        form.error("hasCosts").get.message shouldBe messages.errorRequired
      }
    }

    "passing in an invalid map with an invalid answer to hasCosts" should {
      val map = Map(
        "hasCosts" -> "a",
        "costs" -> "100")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRequired}" in {
        form.error("hasCosts").get.message shouldBe messages.errorRequired
      }
    }

    "passing in an invalid map with 'Yes' and no amount" should {
      val map = Map(
        "hasCosts" -> "Yes",
        "costs" -> "")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.CostsAtLegislationStart.errorNoValue}" in {
        form.error("").get.message shouldBe messages.CostsAtLegislationStart.errorNoValue
      }
    }

    "passing in an invalid map with 'Yes' and a non-numeric amount" should {
      val map = Map(
        "hasCosts" -> "Yes",
        "costs" -> "a")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.CostsAtLegislationStart.errorNoValue}" in {
        form.error("").get.message shouldBe messages.CostsAtLegislationStart.errorNoValue
      }
    }

    "passing in a valid map with 'Yes' and two decimal places" should {
      val map = Map(
        "hasCosts" -> "Yes",
        "costs" -> "100.23")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(CostsAtLegislationStartModel("Yes", Some(100.23)))
      }
    }

    "passing in an invalid map with 'Yes' and three decimal places" should {
      val map = Map(
        "hasCosts" -> "Yes",
        "costs" -> "100.231")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.CostsAtLegislationStart.errorDecimalPlaces}" in {
        form.error("").get.message shouldBe messages.CostsAtLegislationStart.errorDecimalPlaces
      }
    }

    "passing in a valid map with 'Yes' and a value of 0" should {
      val map = Map(
        "hasCosts" -> "Yes",
        "costs" -> "0")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(CostsAtLegislationStartModel("Yes", Some(0)))
      }
    }

    "passing in an invalid map with 'Yes' and a negative value" should {
      val map = Map(
        "hasCosts" -> "Yes",
        "costs" -> "-100")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.CostsAtLegislationStart.errorNegative}" in {
        form.error("").get.message shouldBe messages.CostsAtLegislationStart.errorNegative
      }
    }

    "passing in a valid map with 'Yes' and a value of the maximum" should {
      val map = Map(
        "hasCosts" -> "Yes",
        "costs" -> "1000000000")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(CostsAtLegislationStartModel("Yes", Some(1000000000)))
      }
    }

    "passing in an invalid map with 'Yes' and value above the maximum" should {
      val map = Map(
        "hasCosts" -> "Yes",
        "costs" -> "1000000000.01")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.CostsAtLegislationStart.errorMaximum("1,000,000,000")}" in {
        form.error("").get.message shouldBe messages.CostsAtLegislationStart.errorMaximum("1,000,000,000")
      }
    }

    "passing in an invalid map with multiple failed validation" should {
      val map = Map(
        "hasCosts" -> "Yes",
        "costs" -> "-1.011")
      lazy val form = costsAtLegislationStartForm.bind(map)

      "return an invalid form with two errors" in {
        form.errors.size shouldBe 2
      }
    }
  }
}
