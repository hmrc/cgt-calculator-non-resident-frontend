/*
 * Copyright 2016 HM Revenue & Customs
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

package forms.nonResident

import models.DisposalCostsModel
import assets.MessageLookup.{NonResident => messages}
import forms.DisposalCostsForm
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class DisposalCostsFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing a valid model with 1000.0" should {
      val model = DisposalCostsModel(BigDecimal(1000.0))
      lazy val form = disposalCostsForm.fill(model)

      "return a form with 0 errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing 1000.0" in {
        form.data shouldBe Map("disposalCosts" -> "1000.00")
      }
    }

    "passing a valid map with 2000.0" should {
      val map = Map("disposalCosts" -> "2000.0")
      lazy val form = disposalCostsForm.bind(map)

      "return a form with 0 errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing 2000.0" in {
        form.data shouldBe Map("disposalCosts" -> "2000.0")
      }
    }

    "passing a valid map with two decimal places" should {
      val map = Map("disposalCosts" -> "2000.05")
      lazy val form = disposalCostsForm.bind(map)

      "return a form with 0 errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing 2000.05" in {
        form.data shouldBe Map("disposalCosts" -> "2000.05")
      }
    }

    "passing a valid map with 0" should {
      val map = Map("disposalCosts" -> "0.0")
      lazy val form = disposalCostsForm.bind(map)

      "return a form with 0 errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing 0.0" in {
        form.data shouldBe Map("disposalCosts" -> "0.0")
      }
    }

    "passing a map with three decimal places" should {
      val map = Map("disposalCosts" -> "2000.052")
      lazy val form = disposalCostsForm.bind(map)

      "return a form with 1 errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.DisposalCosts.errorDecimalPlaces}" in {
        form.error("disposalCosts").get.message shouldBe messages.DisposalCosts.errorDecimalPlaces
      }
    }

    "passing a map with an empty value" should {
      val map = Map("disposalCosts" -> "")
      lazy val form = disposalCostsForm.bind(map)

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorRealNumber}" in {
        form.error("disposalCosts").get.message shouldBe messages.errorRealNumber
      }
    }

    "passing a map with a negative number" should {
      val map = Map("disposalCosts" -> "-1000")
      lazy val form = disposalCostsForm.bind(map)

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.DisposalCosts.errorNegativeNumber}" in {
        form.error("disposalCosts").get.message shouldBe messages.DisposalCosts.errorNegativeNumber
      }
    }

    "passing a map with a string" should {
      val map = Map("disposalCosts" -> "test")
      lazy val form = disposalCostsForm.bind(map)

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorRealNumber}" in {
        form.error("disposalCosts").get.message shouldBe messages.errorRealNumber
      }
    }
  }
}
