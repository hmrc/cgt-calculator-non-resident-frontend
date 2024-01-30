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

import assets.KeyLookup.NonResident.{AcquisitionValue => messages}
import common.{Constants, CommonPlaySpec, WithCommonFakeApplication}
import forms.AcquisitionValueForm._
import models.AcquisitionValueModel

class AcquisitionValueFormSpec extends CommonPlaySpec with WithCommonFakeApplication{

  def checkMessageAndError(messageLookup: String, mapping: String): Unit ={
    lazy val form = acquisitionValueForm.bind(Map("acquisitionValue" -> mapping))

    "return a form with errors" in {
      form.hasErrors shouldBe true
    }

    "return 1 error" in {
      form.errors.size shouldBe 1
    }

    s"return an error message $messageLookup" in {
      form.error("acquisitionValue").get.message shouldBe messageLookup
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      lazy val model = AcquisitionValueModel(1000)
      lazy val form = acquisitionValueForm.fill(model)
      form.data shouldBe Map("acquisitionValue" -> "1000")
    }
  }

  "Creating a form using a valid map" should {
    "return a form with the data specific in the model (1000)" in {
      lazy val form = acquisitionValueForm.bind(Map("acquisitionValue" -> "1000"))
      form.value shouldBe Some(AcquisitionValueModel(1000))
    }
  }

  "Creating a form using an invalid map" when {
    "supplied with no data" should {
      val message = messages.errorRequired
      val data = ""

      checkMessageAndError(message, data)
    }

    "supplied with data of the wrong format (incorrect value for acquisitionValue...)" should {
      val message = messages.errorReal
      val data = "junk text"

      checkMessageAndError(message, data)
    }

    "supplied with data containing a negative value" should {
      val message = messages.errorNegative
      val data = "-1000"

      checkMessageAndError(message, data)
    }

    "supplied with data containing a value with too many decimal places" should {
      val message = messages.errorDecimalPlaces
      val data = "1.11111111111"

      checkMessageAndError(message, data)
    }

    "supplied with data containing a value that exceeds the max numeric" should {
      val data = (Constants.maxNumeric + 0.01).toString()
      lazy val form = acquisitionValueForm.bind(Map("acquisitionValue" -> data))
      "return a form with errors" in {
        form.hasErrors shouldBe true
      }
      "return 1 error" in {
        form.errors.size shouldBe 1
      }
      "return the correct error" in {
        form.error("acquisitionValue").get.message shouldBe messages.errorMax
      }
    }
  }
}
