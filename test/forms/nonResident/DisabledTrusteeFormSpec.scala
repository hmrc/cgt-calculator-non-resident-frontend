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

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import models.nonresident.DisabledTrusteeModel
import forms.nonresident.DisabledTrusteeForm._
import assets.MessageLookup.{NonResident => messages}

class DisabledTrusteeFormSpec extends UnitSpec with WithFakeApplication {

  "passing in a valid model" should {
    val model = DisabledTrusteeModel("Yes")
    val form = disabledTrusteeForm.fill(model)

    "return a valid form with no errors" in {
      form.errors.size shouldBe 0
    }

    "return a form containing the data" in {
      form.data shouldBe Map("isVulnerable" -> "Yes")
    }
  }

  "passing in a valid map with Yes" should {
    val map = Map("isVulnerable" -> "Yes")
    lazy val form = disabledTrusteeForm.bind(map)

    "return a valid form with no errors" in {
      form.errors.size shouldBe 0
    }

    "return a form containing the data" in {
      form.value shouldBe Some(DisabledTrusteeModel("Yes"))
    }
  }

  "passing in a valid map with No" should {
    val map = Map("isVulnerable" -> "No")
    lazy val form = disabledTrusteeForm.bind(map)

    "return a valid form with no errors" in {
      form.errors.size shouldBe 0
    }

    "return a form containing the data" in {
      form.value shouldBe Some(DisabledTrusteeModel("No"))
    }
  }

  "passing in an invalid map with no data" should {
    val map = Map("isVulnerable" -> "")
    lazy val form = disabledTrusteeForm.bind(map)

    "return a valid form with no errors" in {
      form.errors.size shouldBe 1
    }

    s"return an error message of '${messages.errorRequired}" in {
      form.error("isVulnerable").get.message shouldBe messages.errorRequired
    }
  }

  "passing in an invalid map with incorrect data" should {
    val map = Map("isVulnerable" -> "a")
    lazy val form = disabledTrusteeForm.bind(map)

    "return a valid form with no errors" in {
      form.errors.size shouldBe 1
    }

    s"return an error message of '${messages.errorRequired}" in {
      form.error("isVulnerable").get.message shouldBe messages.errorRequired
    }
  }
}
