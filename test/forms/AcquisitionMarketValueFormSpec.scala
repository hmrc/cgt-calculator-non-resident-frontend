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

import assets.KeyLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import models.AcquisitionValueModel
import forms.WorthWhenBoughtForLess.worthWhenBoughtForLessForm
import forms.WorthWhenInherited.worthWhenInheritedForm
import forms.WorthWhenGiftedTo.worthWhenGiftedToForm

class AcquisitionMarketValueFormSpec extends CommonPlaySpec with WithCommonFakeApplication {

  "Acquisition Market Value form for worth when bought for less form" when {

    "passing in a valid model" should {
      val model = AcquisitionValueModel(BigDecimal(1500))
      lazy val form = worthWhenBoughtForLessForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("acquisitionMarketValue" -> "1500")
      }
    }

    "passing in a valid map with two decimal places" should {
      val map = Map("acquisitionMarketValue" -> "2500.01")
      lazy val form = worthWhenBoughtForLessForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionValueModel(BigDecimal(2500.01)))
      }
    }

    "passing in an invalid map with three decimal places" should {
      val map = Map("acquisitionMarketValue" -> "1850.456")
      lazy val form = worthWhenBoughtForLessForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenBoughtForLess.errorDecimalPlaces}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenBoughtForLess.errorDecimalPlaces
      }
    }

    "passing in a valid map with a value of 0" should {
      val map = Map("acquisitionMarketValue" -> "0")
      lazy val form = worthWhenBoughtForLessForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionValueModel(BigDecimal(0)))
      }
    }

    "passing in an invalid map with a negative value" should {
      val map = Map("acquisitionMarketValue" -> "-1200")
      lazy val form = worthWhenBoughtForLessForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenBoughtForLess.errorNegativeNumber}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenBoughtForLess.errorNegativeNumber
      }
    }

    "passing in a valid map with the maximum value" should {
      val map = Map("acquisitionMarketValue" -> "1000000000")
      lazy val form = worthWhenBoughtForLessForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionValueModel(BigDecimal(1000000000)))
      }
    }

    "passing in an invalid map with a value above 1 billion and one pence" should {
      val map = Map("acquisitionMarketValue" -> "1000000000.01")
      lazy val form = worthWhenBoughtForLessForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the correct error message" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenBoughtForLess.errorMax
      }
    }

    "passing in an invalid map with an empty value" should {
      val map = Map("acquisitionMarketValue" -> "")
      lazy val form = worthWhenBoughtForLessForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenBoughtForLess.errorReal}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenBoughtForLess.errorReal
      }
    }

    "passing in an invalid map with a non-numeric value" should {
      val map = Map("acquisitionMarketValue" -> "a")
      lazy val form = worthWhenBoughtForLessForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenBoughtForLess.errorReal}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenBoughtForLess.errorReal
      }
    }
  }

  "Acquisition Market Value form for worth when inherited form" when {

    "passing in a valid model" should {
      val model = AcquisitionValueModel(BigDecimal(1500))
      lazy val form = worthWhenInheritedForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("acquisitionMarketValue" -> "1500")
      }
    }

    "passing in a valid map with two decimal places" should {
      val map = Map("acquisitionMarketValue" -> "2500.01")
      lazy val form = worthWhenInheritedForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionValueModel(BigDecimal(2500.01)))
      }
    }

    "passing in an invalid map with three decimal places" should {
      val map = Map("acquisitionMarketValue" -> "1850.456")
      lazy val form = worthWhenInheritedForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenInherited.errorDecimalPlaces}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenInherited.errorDecimalPlaces
      }
    }

    "passing in a valid map with a value of 0" should {
      val map = Map("acquisitionMarketValue" -> "0")
      lazy val form = worthWhenInheritedForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionValueModel(BigDecimal(0)))
      }
    }

    "passing in an invalid map with a negative value" should {
      val map = Map("acquisitionMarketValue" -> "-1200")
      lazy val form = worthWhenInheritedForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenInherited.errorNegativeNumber}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenInherited.errorNegativeNumber
      }
    }

    "passing in a valid map with the maximum value" should {
      val map = Map("acquisitionMarketValue" -> "1000000000")
      lazy val form = worthWhenInheritedForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionValueModel(BigDecimal(1000000000)))
      }
    }

    "passing in an invalid map with a value above 1 billion and one pence" should {
      val map = Map("acquisitionMarketValue" -> "1000000000.01")
      lazy val form = worthWhenInheritedForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the correct error message" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenInherited.errorMax
      }
    }

    "passing in an invalid map with an empty value" should {
      val map = Map("acquisitionMarketValue" -> "")
      lazy val form = worthWhenInheritedForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenInherited.errorReal}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenInherited.errorReal
      }
    }

    "passing in an invalid map with a non-numeric value" should {
      val map = Map("acquisitionMarketValue" -> "a")
      lazy val form = worthWhenInheritedForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenInherited.errorReal}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenInherited.errorReal
      }
    }
  }

  "Acquisition Market Value form for worth when gifted form" when {

    "passing in a valid model" should {
      val model = AcquisitionValueModel(BigDecimal(1500))
      lazy val form = worthWhenGiftedToForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("acquisitionMarketValue" -> "1500")
      }
    }

    "passing in a valid map with two decimal places" should {
      val map = Map("acquisitionMarketValue" -> "2500.01")
      lazy val form = worthWhenGiftedToForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionValueModel(BigDecimal(2500.01)))
      }
    }

    "passing in an invalid map with three decimal places" should {
      val map = Map("acquisitionMarketValue" -> "1850.456")
      lazy val form = worthWhenGiftedToForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenGiftedTo.errorDecimalPlaces}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenGiftedTo.errorDecimalPlaces
      }
    }

    "passing in a valid map with a value of 0" should {
      val map = Map("acquisitionMarketValue" -> "0")
      lazy val form = worthWhenGiftedToForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionValueModel(BigDecimal(0)))
      }
    }

    "passing in an invalid map with a negative value" should {
      val map = Map("acquisitionMarketValue" -> "-1200")
      lazy val form = worthWhenGiftedToForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenGiftedTo.errorNegativeNumber}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenGiftedTo.errorNegativeNumber
      }
    }

    "passing in a valid map with the maximum value" should {
      val map = Map("acquisitionMarketValue" -> "1000000000")
      lazy val form = worthWhenGiftedToForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(AcquisitionValueModel(BigDecimal(1000000000)))
      }
    }

    "passing in an invalid map with a value above 1 billion and one pence" should {
      val map = Map("acquisitionMarketValue" -> "1000000000.01")
      lazy val form = worthWhenGiftedToForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the correct error message" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenGiftedTo.errorMax
      }
    }

    "passing in an invalid map with an empty value" should {
      val map = Map("acquisitionMarketValue" -> "")
      lazy val form = worthWhenGiftedToForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenGiftedTo.errorReal}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenGiftedTo.errorReal
      }
    }

    "passing in an invalid map with a non-numeric value" should {
      val map = Map("acquisitionMarketValue" -> "a")
      lazy val form = worthWhenGiftedToForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.WorthWhenGiftedTo.errorReal}" in {
        form.error("acquisitionMarketValue").get.message shouldBe commonMessages.WorthWhenGiftedTo.errorReal
      }
    }
  }

}
