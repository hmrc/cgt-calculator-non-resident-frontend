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

package forms

import assets.MessageLookup.{NonResident => messages}
import models.CalculationElectionModel
import forms.CalculationElectionForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CalculationElectionFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form with a valid model" should {

      lazy val model = CalculationElectionModel("flat")
      lazy val form = calculationElectionForm.fill(model)

    "return a form with 0 errors" in {
      form.errors.size shouldBe 0
    }

    "return a form with the data specified in the model" in {
      form.value shouldBe Some(model)
    }
  }

  "Creating a form with a valid map" should {

    lazy val form = calculationElectionForm.bind(Map("calculationElection" -> "time"))

    "return a form with the data specified in the map" in {
      form.value shouldBe Some(CalculationElectionModel("time"))
    }

    "return a form with 0 errors" in {
      form.errors.size shouldBe 0
    }
  }

  "Creating a form with an invalid map" should {

    "with an empty model" should {

      lazy val form = calculationElectionForm.bind(Map("calculationElection" -> ""))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.optionReqError}" in {
        form.error("calculationElection").get.message shouldBe messages.optionReqError
      }
    }

    "with a number instead of a string" should {

      lazy val form = calculationElectionForm.bind(Map("calculationElection" -> "9"))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.optionReqError}" in {
        form.error("calculationElection").get.message shouldBe messages.optionReqError
      }

    }

    "with an invalid option" should {

      lazy val form = calculationElectionForm.bind(Map("calculationElection" -> "test data"))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a form with 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.optionReqError}" in {
        form.error("calculationElection").get.message shouldBe messages.optionReqError
      }

    }
  }

}
