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
import forms.BroughtForwardLossesForm._
import models.BroughtForwardLossesModel

class BroughtForwardLossesFormSpec extends CommonPlaySpec with WithCommonFakeApplication{

  "Creating a valid form with a valid model" should {

    "return a form with the specified data" in {
      val model = BroughtForwardLossesModel(isClaiming = true, Some(100))
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
        form.value shouldBe Some(BroughtForwardLossesModel(isClaiming = true, Some(100)))
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
        form.value shouldBe Some(BroughtForwardLossesModel(isClaiming = false, None))
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

      s"return the error message ${messages.errorRequired("broughtForwardLosses")}" in {
        form.error("isClaiming").get.message shouldBe messages.errorRequired("broughtForwardLosses")
      }
    }

    "provided with an invalid response for isClaiming" should {
      val map = Map("isClaiming" -> "a",
        "broughtForwardLoss" -> "")
      lazy val form = broughtForwardLossesForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message ${messages.errorRequired("broughtForwardLosses")}" in {
        form.error("isClaiming").get.message shouldBe messages.errorRequired("broughtForwardLosses")
      }
    }

    "provided with no value for the loss when a Yes is given" should {
      val map = Map("isClaiming" -> "Yes",
        "broughtForwardLoss" -> "")
      lazy val form = broughtForwardLossesForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message ${messages.BroughtForwardLosses.errorRequired}" in {
        form.error("broughtForwardLoss").get.message shouldBe messages.BroughtForwardLosses.errorRequired
      }
    }

    "provided with a non-numeric value for the loss when a Yes is given" should {
      val map = Map("isClaiming" -> "Yes",
        "broughtForwardLoss" -> "a")
      lazy val form = broughtForwardLossesForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message ${messages.BroughtForwardLosses.errorInvalid}" in {
        form.error("broughtForwardLoss").get.message shouldBe messages.BroughtForwardLosses.errorInvalid
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
        form.error("broughtForwardLoss").get.message shouldBe messages.BroughtForwardLosses.errorDecimalPlaces
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
        form.error("broughtForwardLoss").get.message shouldBe messages.BroughtForwardLosses.errorNegative
      }
    }

    "provided with a value above the maximum when a Yes is given" should {
      val map = Map("isClaiming" -> "Yes",
        "broughtForwardLoss" -> "1000000000.01")
      lazy val form = broughtForwardLossesForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the correct error message" in {
        form.error("broughtForwardLoss").get.message shouldBe messages.BroughtForwardLosses.errorTooHigh
      }
    }
  }
}
