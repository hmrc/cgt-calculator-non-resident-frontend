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
import forms.PrivateResidenceReliefForm._
import models.PrivateResidenceReliefModel

class PrivateResidenceReliefFormSpec extends CommonPlaySpec with WithCommonFakeApplication {

  "Creating a form" when {

    "passing in a valid model" should {
      val model = PrivateResidenceReliefModel("Yes", None)
      lazy val form = privateResidenceReliefForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingPRR" -> "Yes")
      }
    }

    "no additional inputs are available and" when {
     lazy val baseForm = privateResidenceReliefForm

      "passing in a valid map with 'Yes', 1000" should {
        val map = Map("isClaimingPRR" -> "Yes", "prrClaimed" -> "1000")
        lazy val form = baseForm.bind(map)

        "return a valid form with no errors" in {
          form.errors.size shouldBe 0
        }

        "return a form containing the data" in {
          form.value shouldBe Some(PrivateResidenceReliefModel("Yes", Some(BigDecimal(1000))))
        }
      }

      "passing in an invalid map with a blank input" should {
        val map = Map("isClaimingPRR" -> "")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.errorRequired("privateResidenceRelief")}" in {
          form.error("isClaimingPRR").get.message shouldBe messages.errorRequired("privateResidenceRelief")
        }
      }

      "passing in an invalid map with an invalid input" should {
        val map = Map("isClaimingPRR" -> "a")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.errorRequired("privateResidenceRelief")}" in {
          form.error("isClaimingPRR").get.message shouldBe messages.errorRequired("privateResidenceRelief")
        }
      }
    }

    "the additional input for prrClaimed is available and" when {
      lazy val baseForm = privateResidenceReliefForm

      "passing in a valid map with 'Yes'" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "prrClaimed" -> "100")
        lazy val form = baseForm.bind(map)

        "return a valid form with no errors" in {
          form.errors.size shouldBe 0
        }

        "return a form containing the data" in {
          form.value shouldBe Some(PrivateResidenceReliefModel("Yes", Some(100)))
        }
      }

      "passing in a valid map with 'No'" should {
        val map = Map(
          "isClaimingPRR" -> "No",
          "prrClaimed" -> "")
        lazy val form = baseForm.bind(map)

        "return a valid form with no errors" in {
          form.errors.size shouldBe 0
        }

        "return a form containing the data" in {
          form.value shouldBe Some(PrivateResidenceReliefModel("No", None))
        }
      }

      "passing in an invalid map with an amount with too many decimal places" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "prrClaimed" -> "100.001")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.PrivateResidenceRelief.errorDecimalPlaces}" in {
          form.error("prrClaimed").get.message shouldBe messages.PrivateResidenceRelief.errorDecimalPlaces
        }
      }

      "passing in an invalid map with an amount with a negative value" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "prrClaimed" -> "-100")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.PrivateResidenceRelief.errorNegative}" in {
          form.error("prrClaimed").get.message shouldBe messages.PrivateResidenceRelief.errorNegative
        }
      }

      "passing in an invalid map with an amount with a value larger than the maximum" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "prrClaimed" -> "1000000001")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return the correct error message" in {
          form.error("prrClaimed").get.message shouldBe "calc.common.error.maxNumericExceeded"
          form.error("prrClaimed").get.args shouldBe Array("1,000,000,000")
        }
      }

      "passing in an invalid map with multiple errors" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "prrClaimed" -> "-1000.5")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }
      }
    }
  }
}
