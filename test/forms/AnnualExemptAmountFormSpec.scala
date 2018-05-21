/*
 * Copyright 2018 HM Revenue & Customs
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

import assets.KeyLookup
import assets.KeyLookup.{NonResident => messages}
import common.Constants
import common.Validation._
import models.AnnualExemptAmountModel
import forms.AnnualExemptAmountForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.play.views.helpers.MoneyPounds

class AnnualExemptAmountFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model" should {
      val model = AnnualExemptAmountModel(BigDecimal(10600))
      val form = annualExemptAmountForm(BigDecimal(10600)).fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("annualExemptAmount" -> "10600")
      }
    }

    "passing in a valid map with two decimal places" should {
      val map = Map("annualExemptAmount" -> "2500.01")
      lazy val form = annualExemptAmountForm(BigDecimal(10600)).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AnnualExemptAmountModel(BigDecimal(2500.01)))
      }
    }

    "passing in an invalid map with three decimal places" should {
      val map = Map("annualExemptAmount" -> "1850.456")
      lazy val form = annualExemptAmountForm(BigDecimal(10600)).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.AnnualExemptAmount.errorDecimalPlaces}" in {
        form.error("annualExemptAmount").get.message shouldBe messages.AnnualExemptAmount.errorDecimalPlaces
      }
    }

    "passing in a valid map with a value of 0" should {
      val map = Map("annualExemptAmount" -> "0")
      lazy val form = annualExemptAmountForm(BigDecimal(10600)).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AnnualExemptAmountModel(BigDecimal(0)))
      }
    }

    "passing in an invalid map with a negative value" should {
      val map = Map("annualExemptAmount" -> "-1200")
      lazy val form = annualExemptAmountForm(BigDecimal(10600)).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.AnnualExemptAmount.errorNegative}" in {
        form.error("annualExemptAmount").get.message shouldBe messages.AnnualExemptAmount.errorNegative
      }
    }

    "passing in a valid map with the maximum value" should {
      val map = Map("annualExemptAmount" -> "10600")
      lazy val form = annualExemptAmountForm(BigDecimal(10600)).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AnnualExemptAmountModel(BigDecimal(10600)))
      }
    }

    "passing in an invalid map with a value above maximum" should {
      val map = Map("annualExemptAmount" -> "11100.01")
      lazy val form = annualExemptAmountForm(BigDecimal(10600)).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of " +
        s"'${maxMonetaryValueConstraint(Constants.maxAllowance)}'" in {
        form.error("annualExemptAmount").get.message shouldBe "calc.common.error.maxNumericExceeded"
      }
    }

    "passing in an invalid map with a value above a different maximum" should {
      val map = Map("annualExemptAmount" -> "12000")
      lazy val form = annualExemptAmountForm(BigDecimal(10000)).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${maxMonetaryValueConstraint(Constants.maxAllowance)}"in {
        form.error("annualExemptAmount").get.message shouldBe "calc.common.error.maxNumericExceeded"
      }
    }

    "passing in an invalid map with an empty value" should {
      val map = Map("annualExemptAmount" -> "")
      lazy val form = annualExemptAmountForm(BigDecimal(10600)).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRealNumber}" in {
        form.error("annualExemptAmount").get.message shouldBe messages.errorRealNumber
      }
    }

    "passing in an invalid map with a non-numeric value" should {
      val map = Map("annualExemptAmount" -> "a")
      lazy val form = annualExemptAmountForm(BigDecimal(10600)).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRealNumber}" in {
        form.error("annualExemptAmount").get.message shouldBe messages.errorRealNumber
      }
    }
  }
}
