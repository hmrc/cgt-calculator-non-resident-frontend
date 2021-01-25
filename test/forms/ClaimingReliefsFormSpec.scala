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

import assets.KeyLookup.NonResident.{ClaimingReliefs => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.ClaimingReliefsForm._
import models.ClaimingReliefsModel

class ClaimingReliefsFormSpec extends CommonPlaySpec with WithCommonFakeApplication {

  "Creating the ClaimingReliefs form from valid inputs" should {

    "return a populated form using .fill" in {
      val model = ClaimingReliefsModel(true)
      val form = claimingReliefsForm.fill(model)

      form.value.get shouldBe ClaimingReliefsModel(true)
    }

    "return a populated form using .bind with an answer of Yes" in {
      val map = Map("isClaimingReliefs" -> "Yes")
      val form = claimingReliefsForm.bind(map)

      form.value.get shouldBe ClaimingReliefsModel(true)
    }

    "return a populated form using .bind with an answer of No" in {
      val map = Map("isClaimingReliefs" -> "No")
      val form = claimingReliefsForm.bind(map)

      form.value.get shouldBe ClaimingReliefsModel(false)
    }
  }

  "Creating the ClaimingReliefs form from invalid inputs" when {

    "supplied with no selection" should {
      val map = Map("isClaimingReliefs" -> "")
      lazy val form = claimingReliefsForm.bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      s"return a form with the error message ${messages.errorMandatory}" in {
        form.error("isClaimingReliefs").get.message shouldBe messages.errorMandatory
      }
    }

    "supplied with an invalid map" should {
      val map = Map("isClaimingReliefs" -> "true")
      lazy val form = claimingReliefsForm.bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      s"return a form with the error message ${messages.errorMandatory}" in {
        form.error("isClaimingReliefs").get.message shouldBe messages.errorMandatory
      }
    }
  }
}