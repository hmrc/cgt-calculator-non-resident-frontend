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

import assets.KeyLookup.{NonResident => commonMessages}
import models.DisposalValueModel
import forms.DisposalValueForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class DisposalValueFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model" should {
      val model = DisposalValueModel(BigDecimal(1500))
      lazy val form = disposalValueForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("disposalValue" -> "1500")
      }
    }

    "passing in a valid map with two decimal places" should {
      val map = Map("disposalValue" -> "2500.01")
      lazy val form = disposalValueForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(DisposalValueModel(BigDecimal(2500.01)))
      }
    }

    "passing in an invalid map with three decimal places" should {
      val map = Map("disposalValue" -> "1850.456")
      lazy val form = disposalValueForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.DisposalValue.errorDecimalPlaces}" in {
        form.error("disposalValue").get.message shouldBe commonMessages.DisposalValue.errorDecimalPlaces
      }
    }

    "passing in a valid map with a value of 0" should {
      val map = Map("disposalValue" -> "0")
      lazy val form = disposalValueForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(DisposalValueModel(BigDecimal(0)))
      }
    }

    "passing in an invalid map with a negative value" should {
      val map = Map("disposalValue" -> "-1200")
      lazy val form = disposalValueForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.DisposalValue.errorNegative}" in {
        form.error("disposalValue").get.message shouldBe commonMessages.DisposalValue.errorNegative
      }
    }

    "passing in a valid map with the maximum value" should {
      val map = Map("disposalValue" -> "1000000000")
      lazy val form = disposalValueForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(DisposalValueModel(BigDecimal(1000000000)))
      }
    }

    "passing in an invalid map with a value above 1 billion and one pence" should {
      val map = Map("disposalValue" -> "1000000000.01")
      lazy val form = disposalValueForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the correct error message" in {
        form.error("disposalValue").get.message shouldBe "calc.common.error.maxNumericExceeded"
        form.error("disposalValue").get.args shouldBe Array("1,000,000,000")
      }
    }

    "passing in an invalid map with an empty value" should {
      val map = Map("disposalValue" -> "")
      lazy val form = disposalValueForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.errorRealNumber}" in {
        form.error("disposalValue").get.message shouldBe commonMessages.errorRealNumber
      }
    }

    "passing in an invalid map with a non-numeric value" should {
      val map = Map("disposalValue" -> "a")
      lazy val form = disposalValueForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.errorRealNumber}" in {
        form.error("disposalValue").get.message shouldBe commonMessages.errorRealNumber
      }
    }
  }
}
