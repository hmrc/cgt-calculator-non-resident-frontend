/*
 * Copyright 2019 HM Revenue & Customs
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
import models.CurrentIncomeModel
import forms.CurrentIncomeForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CurrentIncomeFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model" should {
      val model = CurrentIncomeModel(BigDecimal(1500))
      lazy val form = currentIncomeForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("currentIncome" -> "1500")
      }
    }

    "passing in a valid map with two decimal places" should {
      val map = Map("currentIncome" -> "2500.01")
      lazy val form = currentIncomeForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(CurrentIncomeModel
        (BigDecimal(2500.01)))
      }
    }
    "passing in an invalid map with three decimal places" should {
      val map = Map("currentIncome" -> "1850.456")
      lazy val form = currentIncomeForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.CurrentIncome.errorDecimalPlace}" in {
        form.error("currentIncome").get.message shouldBe messages.CurrentIncome.errorDecimalPlace
      }
    }

    "passing in a valid map with a value of 0" should {
      val map = Map("currentIncome" -> "0")
      lazy val form = currentIncomeForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(CurrentIncomeModel(BigDecimal(0)))
      }
    }

    "passing in an invalid map with a negative value" should {
      val map = Map("currentIncome" -> "-1200")
      lazy val form = currentIncomeForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.CurrentIncome.errorNegative}" in {
        form.error("currentIncome").get.message shouldBe messages.CurrentIncome.errorNegative
      }
    }

    "passing in a valid map with the maximum value" should {
      val map = Map("currentIncome" -> "1000000000")
      lazy val form = currentIncomeForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(CurrentIncomeModel(BigDecimal(1000000000)))
      }
    }

    "passing in an invalid map with a value above 1 billion" should {
      val map = Map("currentIncome" -> "1000000000.01")
      lazy val form = currentIncomeForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the correct error message" in {
        form.error("currentIncome").get.message shouldBe "calc.common.error.maxNumericExceeded"
        form.error("currentIncome").get.args shouldBe Array("1,000,000,000")
      }
    }

    "passing in an invalid map with an empty value" should {
      val map = Map("currentIncome" -> "")
      lazy val form = currentIncomeForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRealNumber}" in {
        form.error("currentIncome").get.message shouldBe messages.errorRealNumber
      }
    }

    "passing in an invalid map with a non-numeric value" should {
      val map = Map("currentIncome" -> "a")
      lazy val form = currentIncomeForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRealNumber}" in {
        form.error("currentIncome").get.message shouldBe messages.errorRealNumber
      }
    }
  }
}
