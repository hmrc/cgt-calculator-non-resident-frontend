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
import models.PrivateResidenceReliefModel
import forms.PrivateResidenceReliefForm._

class PrivateResidenceReliefFormSpec extends CommonPlaySpec with WithCommonFakeApplication {

  "Creating a form" when {

    "passing in a valid model" should {
      val model = PrivateResidenceReliefModel("Yes", None, None)
      lazy val form = privateResidenceReliefForm(false, false).fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingPRR" -> "Yes")
      }
    }

    "no additional inputs are available and" when {
     lazy val baseForm = privateResidenceReliefForm(false, false)

      "passing in a valid map with 'Yes'" should {
        val map = Map("isClaimingPRR" -> "Yes")
        lazy val form = baseForm.bind(map)

        "return a valid form with no errors" in {
          form.errors.size shouldBe 0
        }

        "return a form containing the data" in {
          form.value shouldBe Some(PrivateResidenceReliefModel("Yes", None, None))
        }
      }

      "passing in an invalid map with a blank input" should {
        val map = Map("isClaimingPRR" -> "")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.errorRequired}" in {
          form.error("isClaimingPRR").get.message shouldBe messages.errorRequired
        }
      }

      "passing in an invalid map with an invalid input" should {
        val map = Map("isClaimingPRR" -> "a")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.errorRequired}" in {
          form.error("isClaimingPRR").get.message shouldBe messages.errorRequired
        }
      }
    }

    "the additional input for days before is available and" when {
      lazy val baseForm = privateResidenceReliefForm(true, false)

      "passing in a valid map with 'Yes'" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimed" -> "100")
        lazy val form = baseForm.bind(map)

        "return a valid form with no errors" in {
          form.errors.size shouldBe 0
        }

        "return a form containing the data" in {
          form.value shouldBe Some(PrivateResidenceReliefModel("Yes", Some(100), None))
        }
      }

      "passing in a valid map with 'No'" should {
        val map = Map(
          "isClaimingPRR" -> "No",
          "daysClaimed" -> "")
        lazy val form = baseForm.bind(map)

        "return a valid form with no errors" in {
          form.errors.size shouldBe 0
        }

        "return a form containing the data" in {
          form.value shouldBe Some(PrivateResidenceReliefModel("No", None, None))
        }
      }

      "passing in an invalid map with an amount with too many decimal places" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimed" -> "100.001")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.PrivateResidenceRelief.errorDecimalPlaces}" in {
          form.error("").get.message shouldBe messages.PrivateResidenceRelief.errorDecimalPlaces
        }
      }

      "passing in an invalid map with an amount with a negative value" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimed" -> "-100")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.PrivateResidenceRelief.errorNegative}" in {
          form.error("").get.message shouldBe messages.PrivateResidenceRelief.errorNegative
        }
      }

      "passing in an invalid map with an amount with a value larger than the maximum" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimed" -> "1000000001")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return the correct error message" in {
          form.error("").get.message shouldBe "calc.privateResidenceRelief.error.maxNumericExceeded"
          form.error("").get.args shouldBe Array("1,000,000,000")
        }
      }

      "passing in an invalid map with multiple errors" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimed" -> "-1000.5")
        lazy val form = baseForm.bind(map)

        "return an invalid form with two errors" in {
          form.errors.size shouldBe 2
        }
      }
    }

    "the additional input for days between is available and" when {
      lazy val baseForm = privateResidenceReliefForm(false, true)

      "passing in a valid map with 'Yes'" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimedAfter" -> "100")
        lazy val form = baseForm.bind(map)

        "return a valid form with no errors" in {
          form.errors.size shouldBe 0
        }

        "return a form containing the data" in {
          form.value shouldBe Some(PrivateResidenceReliefModel("Yes", None, Some(100)))
        }
      }

      "passing in a valid map with 'No'" should {
        val map = Map(
          "isClaimingPRR" -> "No",
          "daysClaimedAfter" -> "")
        lazy val form = baseForm.bind(map)

        "return a valid form with no errors" in {
          form.errors.size shouldBe 0
        }

        "return a form containing the data" in {
          form.value shouldBe Some(PrivateResidenceReliefModel("No", None, None))
        }
      }

      "passing in an invalid map with an amount with too many decimal places" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimedAfter" -> "100.001")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.PrivateResidenceRelief.errorDecimalPlaces}" in {
          form.error("").get.message shouldBe messages.PrivateResidenceRelief.errorDecimalPlaces
        }
      }

      "passing in an invalid map with an amount with a negative value" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimedAfter" -> "-100")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.PrivateResidenceRelief.errorNegative}" in {
          form.error("").get.message shouldBe messages.PrivateResidenceRelief.errorNegative
        }
      }

      "passing in an invalid map with an amount with a value larger than the maximum" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimedAfter" -> "1000000001")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return the correct error message" in {
          form.error("").get.message shouldBe "calc.privateResidenceRelief.error.maxNumericExceeded"
          form.error("").get.args shouldBe Array("1,000,000,000")
        }
      }

      "passing in an invalid map with multiple errors" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimedAfter" -> "-1000.5")
        lazy val form = baseForm.bind(map)

        "return an invalid form with two errors" in {
          form.errors.size shouldBe 2
        }
      }
    }

    "both additional inputs are available and" when {
      lazy val baseForm = privateResidenceReliefForm(true, true)

      "passing in a valid map with 'Yes'" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimed" -> "40",
          "daysClaimedAfter" -> "100")
        lazy val form = baseForm.bind(map)

        "return a valid form with no errors" in {
          form.errors.size shouldBe 0
        }

        "return a form containing the data" in {
          form.value shouldBe Some(PrivateResidenceReliefModel("Yes", Some(40), Some(100)))
        }
      }

      "passing in a valid map with 'No'" should {
        val map = Map(
          "isClaimingPRR" -> "No",
          "daysClaimed" -> "",
          "daysClaimedAfter" -> "")
        lazy val form = baseForm.bind(map)

        "return a valid form with no errors" in {
          form.errors.size shouldBe 0
        }

        "return a form containing the data" in {
          form.value shouldBe Some(PrivateResidenceReliefModel("No", None, None))
        }
      }

      "passing in an invalid map with validation failing on the before input" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimed" -> "40.2",
          "daysClaimedAfter" -> "100")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.PrivateResidenceRelief.errorDecimalPlaces}" in {
          form.error("").get.message shouldBe messages.PrivateResidenceRelief.errorDecimalPlaces
        }
      }

      "passing in an invalid map with validation failing on the after input" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimed" -> "40",
          "daysClaimedAfter" -> "-100")
        lazy val form = baseForm.bind(map)

        "return an invalid form with one error" in {
          form.errors.size shouldBe 1
        }

        s"return an error message of '${messages.PrivateResidenceRelief.errorNegative}" in {
          form.error("").get.message shouldBe messages.PrivateResidenceRelief.errorNegative
        }
      }

      "passing in an invalid map with validation failing on both inputs" should {
        val map = Map(
          "isClaimingPRR" -> "Yes",
          "daysClaimed" -> "-40",
          "daysClaimedAfter" -> "100.2")
        lazy val form = baseForm.bind(map)

        "return an invalid form with two errors" in {
          form.errors.size shouldBe 2
        }
      }
    }
  }
}
