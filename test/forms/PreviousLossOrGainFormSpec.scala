/*
 * Copyright 2018 HM Revenue & Customs
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

import assets.KeyLookup.NonResident.{PreviousLossOrGain => messages}
import models.PreviousLossOrGainModel
import forms.PreviousLossOrGainForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PreviousLossOrGainFormSpec extends UnitSpec with WithFakeApplication{
  "Creating the form from a model" should {

    "create an empty form when the model is empty" in {
      val form = previousLossOrGainForm
      form.data.isEmpty shouldBe true
    }

    "create a map with the option 'Loss' when the model contains a 'Loss'" in {
      val model = PreviousLossOrGainModel("Loss")
      val form = previousLossOrGainForm.fill(model)
      form.data.get("previousLossOrGain") shouldBe Some("Loss")
    }

    "create a map with the option 'Gain' when the model contains a 'Gain'" in {
      val model = PreviousLossOrGainModel("Gain")
      val form = previousLossOrGainForm.fill(model)
      form.data.get("previousLossOrGain") shouldBe Some("Gain")
    }

    "create a map with the option 'Neither' when the model contains a 'Neither'" in {
      val model = PreviousLossOrGainModel("Neither")
      val form = previousLossOrGainForm.fill(model)
      form.data.get("previousLossOrGain") shouldBe Some("Neither")
    }


  }

  "Creating the form from an invalid map" when {
    "no data is provided" should {
      lazy val map = Map(("previousLossOrGain", ""))
      lazy val form = previousLossOrGainForm.bind(map)

      "produce a form with errors" in {
        form.hasErrors shouldBe true
      }

      "contain only one error" in {
        form.errors.length shouldBe 1
      }

      s"contain the error message ${messages.mandatoryCheck}" in {
        form.errors.head.message shouldBe messages.mandatoryCheck
      }
    }

    "an invalid string is provided" should {
      lazy val map = Map(("previousLossOrGain", "invalid text"))
      lazy val form = previousLossOrGainForm.bind(map)

      "produce a  form with errors" in {
        form.hasErrors shouldBe true
      }

      "contain only one error" in {
        form.errors.length shouldBe 1
      }

      s"contain the error message ${messages.mandatoryCheck}" in {
        form.errors.head.message shouldBe messages.mandatoryCheck
      }
    }
  }
}
