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

import assets.KeyLookup.{NonResident => messages}
import forms.PersonalAllowanceForm._
import models.PersonalAllowanceModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PersonalAllowanceFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model" should {

      lazy val model = PersonalAllowanceModel(10000)
      lazy val form = personalAllowanceForm(11000).fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("personalAllowance" -> "10000")
      }
    }

    "passing in a valid map" should {

      lazy val map = Map("personalAllowance" -> "9000")
      lazy val form = personalAllowanceForm(11000).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(PersonalAllowanceModel(9000))
      }
    }

    "passing in an invalid map that is empty" should {

      lazy val map = Map("personalAllowance" -> "")
      lazy val form = personalAllowanceForm(11000).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRealNumber}" in {
        form.error("personalAllowance").get.message shouldBe messages.errorRealNumber
      }
    }

    "passing in an invalid map that contains characters" should {

      lazy val map = Map("personalAllowance" -> "asdwq1")
      lazy val form = personalAllowanceForm(11000).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorRealNumber}" in {
        form.error("personalAllowance").get.message shouldBe messages.errorRealNumber
      }
    }

    "passing in an invalid map that is negative" should {

      lazy val map = Map("personalAllowance" -> "-1200")
      lazy val form = personalAllowanceForm(11000).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.PersonalAllowance.errorNegative}" in {
        form.error("personalAllowance").get.message shouldBe messages.PersonalAllowance.errorNegative
      }
    }

    "passing in an invalid map that has too many decimal places" should {

      lazy val map = Map("personalAllowance" -> "12.1328")
      lazy val form = personalAllowanceForm(11000).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.PersonalAllowance.errorDecimalPlaces}" in {
        form.error("personalAllowance").get.message shouldBe messages.PersonalAllowance.errorDecimalPlaces
      }
    }

    "passing in an invalid map that exceeds the personal allowance limit" should {

      lazy val map = Map("personalAllowance" -> "123000123")
      lazy val form = personalAllowanceForm(11000).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return the correct error message" in {
        form.error("personalAllowance").get.message shouldBe "calc.personalAllowance.errorMaxLimit"
        form.error("personalAllowance").get.args shouldBe Array("11,000")
      }
    }
  }

}
