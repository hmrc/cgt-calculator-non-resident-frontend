/*
 * Copyright 2017 HM Revenue & Customs
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

import assets.MessageLookup.{NonResident => commonMessages}
import forms.OtherPropertiesForm._
import models.OtherPropertiesModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class OtherPropertiesFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model with Yes" should {
      lazy val model = OtherPropertiesModel("Yes")
      lazy val form = otherPropertiesForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("otherProperties" -> "Yes")
      }
    }

    "passing in a valid model with No" should {
      lazy val model = OtherPropertiesModel("No")
      lazy val form = otherPropertiesForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("otherProperties" -> "No")
      }
    }

    "passing in an invalid map with an random string instead of Yes or No" should {
      lazy val map = Map("otherProperties" -> "a")
      lazy val form = otherPropertiesForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.errorRequired}" in {
        form.error("otherProperties").get.message shouldBe commonMessages.errorRequired
      }
    }
  }
}
