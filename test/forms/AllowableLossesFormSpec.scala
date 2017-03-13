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

import assets.MessageLookup
import assets.MessageLookup.NonResident.{AllowableLosses => messages}
import models.AllowableLossesModel
import forms.AllowableLossesForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class AllowableLossesFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      lazy val model = AllowableLossesModel("Yes", Some(1000))
      lazy val form = allowableLossesForm.fill(model)
      form.value shouldBe Some(model)
    }
  }

  "Creating a form using a valid map" should {

    "return a form with the data specified in the model (Yes, 1000)" in {
      lazy val form = allowableLossesForm.bind(Map(("isClaimingAllowableLosses", "Yes"), ("allowableLossesAmt", "1000")))
      form.value shouldBe Some(AllowableLossesModel("Yes", Some(1000)))
    }

    "return a form with the data specified in the model (No)" in {
      lazy val form = allowableLossesForm.bind(Map(("isClaimingAllowableLosses", "No")))
      form.value shouldBe Some(AllowableLossesModel("No", None))
    }
  }

  "Creating a form using an invalid map" when {

    "supplied with no data" should {
      lazy val form = allowableLossesForm.bind(Map(("isClaimingAllowableLosses", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error with message ${messages.errorCompulsoryValue}" in {
        form.error("isClaimingAllowableLosses").get.message shouldBe MessageLookup.NonResident.errorRequired
      }
    }

    "supplied with invalid data (incorrect value for isClaiming...)" should {
      lazy val form = allowableLossesForm.bind(Map(("isClaimingAllowableLosses", "a")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error with message ${messages.errorCompulsoryValue}" in {
        form.error("isClaimingAllowableLosses").get.message shouldBe MessageLookup.NonResident.errorRequired
      }
    }

    "supplied with invalid data (no amount supplied)" should {
      lazy val form = allowableLossesForm.bind(Map(("isClaimingAllowableLosses", "Yes"), ("allowableLossesAmt", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error with message ${messages.errorCompulsoryValue}" in {
        form.error("").get.message shouldBe messages.errorCompulsoryValue
      }
    }

    "supplied with invalid data (to many decimal places)" should {
      lazy val form = allowableLossesForm.bind(Map(("isClaimingAllowableLosses", "Yes"), ("allowableLossesAmt", "21366.123512")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error with message ${messages.errorTooManyDecimals}" in {
        form.error("").get.message shouldBe messages.errorTooManyDecimals
      }
    }

    "supplied with invalid data (minimum amount not met)" should {
      lazy val form = allowableLossesForm.bind(Map(("isClaimingAllowableLosses", "Yes"), ("allowableLossesAmt", "-1236")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error with message ${messages.errorMinimumAmount}" in {
        form.error("").get.message shouldBe messages.errorMinimumAmount
      }
    }

    "supplied with invalid data (maximum exceeded)" should {
      lazy val form = allowableLossesForm.bind(Map(("isClaimingAllowableLosses", "Yes"), ("allowableLossesAmt", "51237216356172352134")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error with message ${MessageLookup.NonResident.maximumAmount}" in {
        form.error("").get.message shouldBe MessageLookup.NonResident.maximumAmount
      }
    }

    "supplied with valid isClaiming (no) but an invalid amount" should {
      lazy val form = allowableLossesForm.bind(Map(("isClaimingAllowableLosses", "No"), ("allowableLossesAmt", "51237216356172352134")))

      "return a form with errors" in {
        form.hasErrors shouldBe false
      }

      "return 1 error" in {
        form.errors.size shouldBe 0
      }
    }
  }

}
