/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.RebasedCostsForm._
import models.RebasedCostsModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class RebasedCostsFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model" should {
      val model = RebasedCostsModel("Yes", Some(BigDecimal(1500)))
      lazy val form = rebasedCostsForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map(
          "hasRebasedCosts" -> "Yes",
          "rebasedCosts" -> "1500")
      }
    }

    "passing in a valid map with 'No'" should {
      val map = Map(
        "hasRebasedCosts" -> "No",
        "rebasedCosts" -> "")
      lazy val form = rebasedCostsForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(RebasedCostsModel("No", None))
      }
    }

    "passing in a valid map with 'No' and an invalid amount" should {
      val map = Map(
        "hasRebasedCosts" -> "No",
        "rebasedCosts" -> "50.234")
      lazy val form = rebasedCostsForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(RebasedCostsModel("No", Some(50.234)))
      }
    }

    "passing in an invalid map with no answer to hasRebasedCosts" should {
      val map = Map(
        "hasRebasedCosts" -> "",
        "rebasedCosts" -> "100")
      lazy val form = rebasedCostsForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRequired}" in {
        form.error("hasRebasedCosts").get.message shouldBe messages.errorRequired
      }
    }

    "passing in an invalid map with an invalid answer to hasRebasedCosts" should {
      val map = Map(
        "hasRebasedCosts" -> "a",
        "rebasedCosts" -> "100")
      lazy val form = rebasedCostsForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRequired}" in {
        form.error("hasRebasedCosts").get.message shouldBe messages.errorRequired
      }
    }

    "passing in an invalid map with 'Yes' and no amount" should {
      val map = Map(
        "hasRebasedCosts" -> "Yes",
        "rebasedCosts" -> "")
      lazy val form = rebasedCostsForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.RebasedCosts.errorNoValue}" in {
        form.error("").get.message shouldBe messages.RebasedCosts.errorNoValue
      }
    }

    "passing in an invalid map with 'Yes' and a non-numeric amount" should {
      val map = Map(
        "hasRebasedCosts" -> "Yes",
        "rebasedCosts" -> "a")
      lazy val form = rebasedCostsForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.RebasedCosts.errorNoValue}" in {
        form.error("").get.message shouldBe messages.RebasedCosts.errorNoValue
      }
    }

    "passing in a valid map with 'Yes' and two decimal places" should {
      val map = Map(
        "hasRebasedCosts" -> "Yes",
        "rebasedCosts" -> "100.23")
      lazy val form = rebasedCostsForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(RebasedCostsModel("Yes", Some(100.23)))
      }
    }

    "passing in an invalid map with 'Yes' and three decimal places" should {
      val map = Map(
        "hasRebasedCosts" -> "Yes",
        "rebasedCosts" -> "100.231")
      lazy val form = rebasedCostsForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.RebasedCosts.errorDecimalPlaces}" in {
        form.error("").get.message shouldBe messages.RebasedCosts.errorDecimalPlaces
      }
    }

    "passing in a valid map with 'Yes' and a value of 0" should {
      val map = Map(
        "hasRebasedCosts" -> "Yes",
        "rebasedCosts" -> "0")
      lazy val form = rebasedCostsForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(RebasedCostsModel("Yes", Some(0)))
      }
    }

    "passing in an invalid map with 'Yes' and a negative value" should {
      val map = Map(
        "hasRebasedCosts" -> "Yes",
        "rebasedCosts" -> "-100")
      lazy val form = rebasedCostsForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.RebasedCosts.errorNegative}" in {
        form.error("").get.message shouldBe messages.RebasedCosts.errorNegative
      }
    }

    "passing in a valid map with 'Yes' and a value of the maximum" should {
      val map = Map(
        "hasRebasedCosts" -> "Yes",
        "rebasedCosts" -> "1000000000")
      lazy val form = rebasedCostsForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(RebasedCostsModel("Yes", Some(1000000000)))
      }
    }

    "passing in an invalid map with 'Yes' and value above the maximum" should {
      val map = Map(
        "hasRebasedCosts" -> "Yes",
        "rebasedCosts" -> "1000000000.01")
      lazy val form = rebasedCostsForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the correct error message" in {
        form.error("").get.message shouldBe "calc.common.error.maxNumericExceeded"
        form.error("").get.args shouldBe Array("1,000,000,000")
      }
    }

    "passing in an invalid map with multiple failed validation" should {
      val map = Map(
        "hasRebasedCosts" -> "Yes",
        "rebasedCosts" -> "-1.011")
      lazy val form = rebasedCostsForm.bind(map)

      "return an invalid form with two errors" in {
        form.errors.size shouldBe 2
      }
    }
  }
}
