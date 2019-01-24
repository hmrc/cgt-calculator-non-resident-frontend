/*
 * Copyright 2019 HM Revenue & Customs
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
import forms.SoldForLessForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class SoldForLessFormSpec extends UnitSpec with WithFakeApplication {

  "Creating the form" should {
    "with an empty model" should {

      lazy val form = soldForLessForm

      "create an empty form" in {
        form.data.isEmpty shouldEqual true
      }


      "with an valid 'yes' map" should {

        lazy val form = soldForLessForm.bind(Map("soldForLess" -> "Yes"))

        "create a form with the data from the model" in {
          form.data("soldForLess") shouldEqual "Yes"
        }

        "raise no form error" in {
          form.hasErrors shouldBe false
        }

        "raise 0 form errors" in {
          form.errors.length shouldBe 0
        }
      }

      "with a valid 'no' map" should {

        lazy val form = soldForLessForm.bind(Map("soldForLess" -> "No"))

        "create a form with the data from the model" in {
          form.data("soldForLess") shouldEqual "No"
        }

        "raise no form error" in {
          form.hasErrors shouldBe false
        }

        "raise 0 form errors" in {
          form.errors.length shouldBe 0
        }

        "supplied with no data for option" should {

          lazy val form = soldForLessForm.bind(Map("soldForLess" -> ""))

          "raise form error" in {
            form.hasErrors shouldBe true
          }

          "raise 1 form error" in {
            form.errors.length shouldBe 1
          }

          "associate the correct error message to the error" in {
            form.error("soldForLess").get.message shouldBe commonMessages.errorRequired
          }
        }

        "supplied with invalid data for option" should {

          lazy val form = soldForLessForm.bind(Map("soldForLess" -> "asdas"))

          "raise form error" in {
            form.hasErrors shouldBe true
          }

          "raise 1 form error" in {
            form.errors.length shouldBe 1
          }

          "associate the correct error message to the error" in {
            form.error("soldForLess").get.message shouldBe commonMessages.errorRequired
          }
        }
      }
    }
  }
}
