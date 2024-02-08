/*
 * Copyright 2024 HM Revenue & Customs
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

import forms.WhoDidYouGiveItToForm._
import models.WhoDidYouGiveItToModel
import assets.KeyLookup.WhoDidYouGiveItTo
import common.{CommonPlaySpec, WithCommonFakeApplication}

class WhoDidYouGiveItToFormSpec extends CommonPlaySpec with WithCommonFakeApplication {
  "Creating the form from an empty model" should {

    "create an empty form when the model is empty" in {
      lazy val form = whoDidYouGiveItToForm
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      lazy val form = whoDidYouGiveItToForm.fill(WhoDidYouGiveItToModel("Charity"))
      form.data("whoDidYouGiveItTo") shouldBe "Charity"
    }

    "return a form with the data specified from the map" in {
      lazy val form = whoDidYouGiveItToForm.bind(Map("whoDidYouGiveItTo" -> "Charity"))
      form.data("whoDidYouGiveItTo") shouldBe "Charity"
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for who did you give it to" should {

      lazy val form = whoDidYouGiveItToForm.bind(Map("whoDidYouGiveItTo" -> ""))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise only one error" in {
        form.errors.length shouldBe 1
      }

      s"error with message '${WhoDidYouGiveItTo.errormandatory}" in {
        form.error("whoDidYouGiveItTo").get.message shouldBe WhoDidYouGiveItTo.errormandatory
      }
    }

  }

  "supplied with an invalid option for who did you give it to" should {
    lazy val form = whoDidYouGiveItToForm.bind(Map("whoDidYouGiveItTo" -> "Blah"))

    "raise a form error" in {
      form.hasErrors shouldBe true
    }

    "raise only one error" in {
      form.errors.length shouldBe 1
    }

    s"error with message '${WhoDidYouGiveItTo.errormandatory}" in {
      form.error("whoDidYouGiveItTo").get.message shouldBe WhoDidYouGiveItTo.errormandatory
    }

    "throw an error when supplied with incorrect mappings" in {
      lazy val form = whoDidYouGiveItToForm.bind(Map(("whoDidYouGiveItTo", "Something")))

      form.hasErrors shouldBe true
    }
  }

  "throw an error when supplied with an empty value" in {
    lazy val form = whoDidYouGiveItToForm.bind(Map(("whoDidYouGiveItTo", "")))

    form.hasErrors shouldBe true
  }

  "not throw an error when supplied with correct/valid mapping for Spouse option" in {
    lazy val form = whoDidYouGiveItToForm.bind(Map(("whoDidYouGiveItTo", "Spouse")))
    form.hasErrors shouldBe false
  }

  "not throwing an error when supplied with the correct/valid mappings for Charity option" in {
    lazy val form = whoDidYouGiveItToForm.bind(Map(("whoDidYouGiveItTo", "Charity")))
    form.hasErrors shouldBe false
  }

  "not throwing an error when supplied with the correct/valid mappings for Other option" in {
    lazy val form = whoDidYouGiveItToForm.bind(Map(("whoDidYouGiveItTo", "Other")))

    form.hasErrors shouldBe false
  }
}

