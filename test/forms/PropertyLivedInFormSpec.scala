/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.PropertyLivedInForm._
import models.PropertyLivedInModel

class PropertyLivedInFormSpec extends CommonPlaySpec with WithCommonFakeApplication {

  "Creating the PropertyLivedIn form from valid inputs" should {

    "return a populated form using .fill" in {
      val model = PropertyLivedInModel(true)
      val form = propertyLivedInForm.fill(model)

      form.value.get shouldBe PropertyLivedInModel(true)
    }

    "return a populated form using .bind with an answer of Yes" in {
      val form = propertyLivedInForm.bind(Map(("propertyLivedIn", "Yes")))

      form.value.get shouldBe PropertyLivedInModel(true)
    }

    "return a populated form using .bind with an answer of No" in {
      val form = propertyLivedInForm.bind(Map(("propertyLivedIn", "No")))

      form.value.get shouldBe PropertyLivedInModel(false)
    }
  }

  "Creating the PropertyLivedIn form from invalid inputs" when {

    "supplied with no selection" should {
      lazy val form = propertyLivedInForm.bind(Map(("propertyLivedIn", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      s"return a form with the error message ${messages.errorRequired("propertyLivedIn")}" in {
        form.error("propertyLivedIn").get.message shouldBe messages.errorRequired("propertyLivedIn")
      }
    }

    "supplied with an incorrect selection" should {
      lazy val form = propertyLivedInForm.bind(Map(("propertyLivedIn", "true")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      s"return a form with the error message ${messages.errorRequired("propertyLivedIn")}" in {
        form.error("propertyLivedIn").get.message shouldBe messages.errorRequired("propertyLivedIn")
      }
    }
  }
}