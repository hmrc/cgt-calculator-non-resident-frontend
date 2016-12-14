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

import models.BroughtForwardLossesModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{NonResident => messages}
import forms.BroughtForwardLossesForm

class BroughtForwardLossesFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a valid form with a valid model" should {

    "return a form with the specified data" in {
      val model = BroughtForwardLossesModel(true, Some(100))
      val form = broughtForwardLossesForm.fill(model)

      form.data shouldBe Map("isClaiming" -> "Yes",
        "broughtForwardLoss" -> "100")
    }
  }

  "Creating a valid form from a valid map" when {

    "provided with a value of Yes and a loss" should {
      val map = Map("isClaiming" -> "Yes",
        "broughtForwardLoss" -> "100")
      val form = broughtForwardLossesForm.bind(map)

      "return a form without errors" in {
        form.errors.size shouldBe 0
      }

      "return a model with the mapped data" in {
        form.value shouldBe Some(BroughtForwardLossesModel(true, Some(100)))
      }
    }

    "provided with a value of No" should {
      val map = Map("isClaiming" -> "No",
        "broughtForwardLoss" -> "")
      val form = broughtForwardLossesForm.bind(map)

      "return a form without errors" in {
        form.errors.size shouldBe 0
      }

      "return a model with the mapped data" in {
        form.value shouldBe Some(BroughtForwardLossesModel(false, None))
      }
    }
  }

  "Creating an invalid form" when {

    "provided with no response for isClaiming" should {
      val map = Map("isClaiming" -> "",
        "broughtForwardLoss" -> "")
      lazy val form = broughtForwardLossesForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message ${messages.errorRequired}" in {
        form.error("isClaiming").get.message shouldBe messages.errorRequired
      }
    }

    "provided with an invalid response for isClaiming" should {
      val map = Map("isClaiming" -> "a",
        "broughtForwardLoss" -> "")
      lazy val form = broughtForwardLossesForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message ${messages.errorRequired}" in {
        form.error("isClaiming").get.message shouldBe messages.errorRequired
      }
    }

    "provided with no value for the loss when a Yes is given" should {
      val map = Map("isClaiming" -> "Yes",
        "broughtForwardLoss" -> "")
      lazy val form = broughtForwardLossesForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message ${messages.errorRealNumber}" in {
        form.error("").get.message shouldBe messages.errorRealNumber
      }
    }

    "provided with a non-numeric value for the loss when a Yes is given" should {
      val map = Map("isClaiming" -> "Yes",
        "broughtForwardLoss" -> "a")
      lazy val form = broughtForwardLossesForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message ${messages.errorRealNumber}" in {
        form.error("").get.message shouldBe messages.errorRealNumber
      }
    }

    "provided with a value with three decimal places when a Yes is given" should {
      val map = Map("isClaiming" -> "Yes",
        "broughtForwardLoss" -> "100.001")
      lazy val form = broughtForwardLossesForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message ${messages.BroughtForwardLosses.errorDecimalPlaces}" in {
        form.error("").get.message shouldBe messages.BroughtForwardLosses.errorDecimalPlaces
      }
    }

    "provided with a value with a negative value when a Yes is given" should {
      val map = Map("isClaiming" -> "Yes",
        "broughtForwardLoss" -> "-0.01")
      lazy val form = broughtForwardLossesForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message ${messages.BroughtForwardLosses.errorNegative}" in {
        form.error("").get.message shouldBe messages.BroughtForwardLosses.errorNegative
      }
    }

    "provided with a value above the maximum when a Yes is given" should {
      val map = Map("isClaiming" -> "Yes",
        "broughtForwardLoss" -> "1000000000.01")
      lazy val form = broughtForwardLossesForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message ${messages.BroughtForwardLosses.errorMaximum("1,000,000,000")}" in {
        form.error("").get.message shouldBe messages.BroughtForwardLosses.errorMaximum("1,000,000,000")
      }
    }
  }
}
