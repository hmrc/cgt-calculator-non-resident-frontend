/*
 * Copyright 2022 HM Revenue & Customs
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

import assets.KeyLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import models.SoldOrGivenAwayModel
import forms.SoldOrGivenAwayForm._

class SoldOrGivenAwayFormSpec extends CommonPlaySpec with WithCommonFakeApplication{

  "Sell Or Give Away Form" when {

    "passing in a true valid model" should {
      val model = SoldOrGivenAwayModel(true)
      val form = soldOrGivenAwayForm.fill(model)

      "return a form with 0 errors" in {
        form.errors.size shouldBe 0
      }

      "return the correct data" in {
        form.data shouldBe Map("soldIt" -> "Yes")
      }
    }

    "passing in a valid yes map" should {
      val map = Map("soldIt" -> "Yes")
      lazy val form = soldOrGivenAwayForm.bind(map)

      "return a form with 0 errors" in {
        form.errors.size shouldBe 0
      }

      "return the correct data" in {
        form.value shouldBe Some(SoldOrGivenAwayModel(true))
      }
    }

    "passing in a valid no map" should {
      val map = Map("soldIt" -> "No")
      lazy val form = soldOrGivenAwayForm.bind(map)

      "return a form with 0 errors" in {
        form.errors.size shouldBe 0
      }

      "return the correct data" in {
        form.value shouldBe Some(SoldOrGivenAwayModel(false))
      }
    }


    "passing in invalid data" should {
      val map = Map("soldIt" -> "999")
      lazy val form = soldOrGivenAwayForm.bind(map)

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message of ${commonMessages.errorRequired("soldOrGivenAway")}" in {
        form.error("soldIt").get.message shouldBe commonMessages.errorRequired("soldOrGivenAway")
      }
    }

    "passing in an empty map" should {
      val map = Map("soldIt" -> "")
      lazy val form = soldOrGivenAwayForm.bind(map)

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return the error message of ${commonMessages.errorRequired("soldOrGivenAway")}" in {
        form.error("soldIt").get.message shouldBe commonMessages.errorRequired("soldOrGivenAway")
      }
    }
  }
}
