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

import forms.nonresident.WorthBeforeLegislationStartForm._
import models.nonresident.WorthBeforeLegislationStartModel
import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{AcquisitionMarketValue => messages}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class WorthBeforeLegislationStartFormSpec extends UnitSpec with WithFakeApplication {

  "The Worth Before Legislation Start Form" when {

    "passing in a valid model" should {
      val model = WorthBeforeLegislationStartModel(250000)
      lazy val form = worthBeforeLegislationStartForm.fill(model)

      "return a form with 0 errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing 250000" in {
        form.data shouldBe Map("worthBeforeLegislationStart" -> "250000")
      }
    }

    "passing in a valid map" should {
      lazy val form = worthBeforeLegislationStartForm.bind(Map("worthBeforeLegislationStart" -> "125000"))

      "return a form with 0 errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing 125000" in {
        form.value shouldBe Some(WorthBeforeLegislationStartModel(125000))
      }
    }

    "passing in an invalid map with 'Yes'" should {
      lazy val form = worthBeforeLegislationStartForm.bind(Map("worthBeforeLegislationStart" -> "Yes"))

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${commonMessages.errorRealNumber}" in {
        form.error("worthBeforeLegislationStart").get.message shouldBe commonMessages.errorRealNumber
      }
    }

    "passing in an invalid map with a number too large" should {
      lazy val form = worthBeforeLegislationStartForm.bind(Map("worthBeforeLegislationStart" -> "100000000000"))

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${commonMessages.maximumAmount}" in {
        form.error("worthBeforeLegislationStart").get.message shouldBe commonMessages.maximumAmount
      }
    }

    "passing in an invalid map with a number with too many decimal places" should {
      lazy val form = worthBeforeLegislationStartForm.bind(Map("worthBeforeLegislationStart" -> "100.000"))

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorDecimalPlaces}" in {
        form.error("worthBeforeLegislationStart").get.message shouldBe messages.errorDecimalPlaces
      }
    }

    "passing in an invalid map with a negative number" should {
      lazy val form = worthBeforeLegislationStartForm.bind(Map("worthBeforeLegislationStart" -> "-100.00"))

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorNegativeNumber}" in {
        form.error("worthBeforeLegislationStart").get.message shouldBe messages.errorNegativeNumber
      }
    }
  }
}
