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
import forms.ImprovementsForm._
import models.ImprovementsModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class ImprovementsFormSpec extends UnitSpec with WithFakeApplication{

  "Creating a form" when {

    "passing a in a valid model" should {
      val model = ImprovementsModel("Yes", Some(1000.0), Some(1000.0))
      lazy val form = improvementsForm(true).fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "1000.0", "improvementsAmtAfter" -> "1000.0")
      }
    }

    "passing a in a valid yes map" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "3000.0", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm(true).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value.get shouldBe ImprovementsModel("Yes", Some(3000.0), Some(3000.0))
      }
    }

    "passing a in a valid no map" should {
      val map = Map("isClaimingImprovements" -> "No", "improvementsAmt" -> "", "improvementsAmtAfter" -> "")
      lazy val form = improvementsForm(true).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value.get shouldBe ImprovementsModel("No", None, None)
      }
    }

    "passing a in a valid map with two decimal places" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "3000.05", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm(true).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value.get shouldBe ImprovementsModel("Yes", Some(3000.05), Some(3000.00))
      }
    }

    "passing in a valid map with a improvementsAmt on the max amount" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "1000000000.00", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe false
      }

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value.get shouldBe ImprovementsModel("Yes", Some(1000000000.00), Some(3000.00))
      }
    }

    "passing in a valid map with a improvementsAmt on the max amount and improvementsAmtAfter is empty" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "1000000000.00", "improvementsAmtAfter" -> "")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe false
      }

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value.get shouldBe ImprovementsModel("Yes", Some(BigDecimal(1000000000.00)), None)
      }
    }

    "passing in a valid map with a 'isClaimingImprovements' no and an error in improvementsAmt" should {
      val map = Map("isClaimingImprovements" -> "No", "improvementsAmt" -> "testData", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe false
      }

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value.get shouldBe ImprovementsModel("No", None, Some(3000.0))
      }
    }

    "passing in a valid map with a 'isClaimingImprovements' yes actionspec" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "12045", "improvementsAmtAfter" -> "12045")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe false
      }

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value.get shouldBe ImprovementsModel("Yes", Some(12045.0), Some(12045.0))
      }
    }

    "passing in a valid map with a 'isClaimingImprovements' no and an error in improvementsAmtAfter" should {
      val map = Map("isClaimingImprovements" -> "No", "improvementsAmt" -> "3000.0", "improvementsAmtAfter" -> "testData")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe false
      }

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value.get shouldBe ImprovementsModel("No", Some(3000.0), None)
      }
    }

    "selecting 'Yes' but not supplying any amounts when both improvements are shown" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "", "improvementsAmtAfter" -> "")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.excessDecimalPlacesError} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.noValueSuppliedError
      }
    }

    "selecting 'Yes' but not supplying a before amount when only before amount is shown" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "", "improvementsAmtAfter" -> "1.11")
      lazy val form = improvementsForm(false).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.excessDecimalPlacesError} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.noValueSuppliedError
      }
    }

    "passing in an invalid map with three decimal places for improvementsAmt" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "3000.051", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.excessDecimalPlacesError} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.excessDecimalPlacesError
      }
    }

    "passing in an invalid map with three decimal places for improvementsAmtAfter" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "3000.51", "improvementsAmtAfter" -> "3000.009")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.excessDecimalPlacesError} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.excessDecimalPlacesError
      }
    }

    "passing in an invalid map with a negative number for improvementsAmt" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "-3000.01", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.negativeValueError} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.negativeValueError
      }
    }

    "passing in an invalid map with a negative number for improvementsAmtAfter" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "3000.01", "improvementsAmtAfter" -> "-3000.0")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.negativeValueError} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.negativeValueError
      }
    }

    "passing in a invalid map with a improvementsAmt over the max amount" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "1000000000.01", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the correct error message" in {
        form.error("").get.message shouldBe "calc.common.error.maxNumericExceeded"
        form.error("").get.args shouldBe Array("1,000,000,000")
      }
    }

    "passing in a invalid map with a improvementsAmtAfter over the max amount" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "1000.01", "improvementsAmtAfter" -> "1000000000.01")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the correct error message" in {
        form.error("").get.message shouldBe "calc.common.error.maxNumericExceeded"
        form.error("").get.args shouldBe Array("1,000,000,000")
      }
    }

    "passing in a invalid map with a improvementsAmtAfter over the max amount and having three decimal places" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "1000.01", "improvementsAmtAfter" -> "1000000000.013")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 2
      }

      s"return an error message of ${messages.Improvements.excessDecimalPlacesError} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.excessDecimalPlacesError
      }
    }

    "passing in a invalid map with no improvementsAmt or improvementsAmtAfter" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "", "improvementsAmtAfter" -> "")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.noValueSuppliedError} containing the data" in {
        form.errors.head.message shouldBe messages.Improvements.noValueSuppliedError
      }
    }

    "passing in a invalid map with a string for improvementsAmt" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "testData", "improvementsAmtAfter" -> "")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.noValueSuppliedError} containing the data" in {
        form.errors.head.message shouldBe messages.Improvements.noValueSuppliedError
      }
    }

    "passing in a invalid map with a string for improvementsAmtAfter" should {
      val map = Map("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "", "improvementsAmtAfter" -> "testData")
      lazy val form = improvementsForm(true).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.noValueSuppliedError} containing the data" in {
        form.errors.head.message shouldBe messages.Improvements.noValueSuppliedError
      }
    }
  }
}
