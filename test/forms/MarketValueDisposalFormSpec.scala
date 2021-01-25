/*
 * Copyright 2021 HM Revenue & Customs
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

import assets.KeyLookup.NonResident.{MarketValue => marketValueMessages}
import assets.KeyLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.MarketValueGaveAwayForm._
import forms.MarketValueWhenSoldForm._
import models.DisposalValueModel

class MarketValueDisposalFormSpec extends CommonPlaySpec with WithCommonFakeApplication {

  "Creating a form using an empty model" should {
    lazy val form = marketValueWhenGaveAwayForm

    "return an empty string for amount" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      val model = DisposalValueModel(1)
      val form = marketValueWhenGaveAwayForm.fill(model)
      form.data("disposalValue") shouldBe "1"
    }
  }

  "Creating a form using an invalid post" when {

    "supplied with no data for amount" should {

      lazy val form = marketValueWhenGaveAwayForm.bind(Map("disposalValue" -> ""))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("disposalValue").get.message shouldBe messages.errorRealNumber
      }
    }

    "supplied with empty space for amount" should {

      lazy val form = marketValueWhenGaveAwayForm.bind(Map("disposalValue" -> "  "))

      "raise form error" in {
        form.hasErrors shouldBe true
      }


      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("disposalValue").get.message shouldBe messages.errorRealNumber
      }
    }

    "supplied with non numeric input for amount" should {

      lazy val form = marketValueWhenGaveAwayForm.bind(Map("disposalValue" -> "a"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("disposalValue").get.message shouldBe messages.errorRealNumber
      }
    }

    "supplied with an amount with 3 numbers after the decimal" when {

      "using the marketValueWhenGaveAwayForm" should {
        lazy val form = marketValueWhenGaveAwayForm.bind(Map("disposalValue" -> "1.000"))

        "raise form error" in {
          form.hasErrors shouldBe true
        }

        "raise 1 form error" in {
          form.errors.length shouldBe 1
        }

        "associate the correct error message to the error" in {
          form.error("disposalValue").get.message shouldBe marketValueMessages.disposalErrorDecimalPlacesGaveAway
        }
      }

      "using the marketValueWhenSold" should {
        lazy val form = marketValueWhenSoldForm.bind(Map("disposalValue" -> "1.000"))

        "raise form error" in {
          form.hasErrors shouldBe true
        }

        "raise 1 form error" in {
          form.errors.length shouldBe 1
        }

        "associate the correct error message to the error" in {
          form.error("disposalValue").get.message shouldBe marketValueMessages.disposalErrorDecimalPlacesSold
        }
      }
    }

    "supplied with an amount that's greater than the max" should {

      lazy val form = marketValueWhenGaveAwayForm.bind(Map("disposalValue" -> "1000000000.01"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("disposalValue").get.message shouldBe "calc.common.error.maxNumericExceeded"
        form.error("disposalValue").get.args shouldBe Array("1,000,000,000")
      }
    }

    "supplied with an amount that's less than the zero" when {

      "using the marketValueWhenGaveAwayForm" should {
        lazy val form = marketValueWhenGaveAwayForm.bind(Map("disposalValue" -> "-0.01"))

        "raise form error" in {
          form.hasErrors shouldBe true
        }

        "raise 1 form error" in {
          form.errors.length shouldBe 1
        }

        "associate the correct error message to the error" in {
          form.error("disposalValue").get.message shouldBe marketValueMessages.errorNegativeGaveAway
        }
      }

      "using the marketValueWhenSoldForm" should {
        lazy val form = marketValueWhenSoldForm.bind(Map("disposalValue" -> "-0.01"))
        "raise form error" in {
          form.hasErrors shouldBe true
        }

        "raise 1 form error" in {
          form.errors.length shouldBe 1
        }

        "associate the correct error message to the error" in {
          form.error("disposalValue").get.message shouldBe marketValueMessages.errorNegativeSold
        }
      }
    }
  }

  "Creating a form using a valid post" when {

    "supplied with valid amount" should {

      lazy val form = marketValueWhenGaveAwayForm.bind(Map("disposalValue" -> "1"))

      "build a model with the correct amount" in {
        form.value.get shouldBe DisposalValueModel(BigDecimal(1))
      }

      "not raise form error" in {
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount with 1 number after the decimal" should {
      "not raise form error" in {
        val form = marketValueWhenGaveAwayForm.bind(Map("disposalValue" -> "1.1"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount with 2 numbers after the decimal" should {
      "not raise form error" in {
        val form = marketValueWhenGaveAwayForm.bind(Map("disposalValue" -> "1.11"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount that's equal to the max" should {
      "not raise form error" in {
        val form = marketValueWhenGaveAwayForm.bind(Map("disposalValue" -> "1000000000"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount that's equal to the min" should {
      "not raise form error" in {
        val form = marketValueWhenGaveAwayForm.bind(Map("disposalValue" -> "0"))
        form.hasErrors shouldBe false
      }
    }
  }
}
