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


import assets.MessageLookup.NonResident.{HowBecameOwner => messages}
import models.HowBecameOwnerModel
import forms.HowBecameOwnerForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class HowBecameOwnerFormSpec extends UnitSpec with WithFakeApplication {

  "Creating the form from a model" should {

    "create an empty form when the model is empty" in {
      val form = howBecameOwnerForm
      form.data.isEmpty shouldBe true
    }

    "create a map with the option 'Bought' when the model contains a 'Bought'" in {
      val model = HowBecameOwnerModel("Bought")
      val form = howBecameOwnerForm.fill(model)
      form.data.get("gainedBy") shouldBe Some("Bought")
    }

    "create a map with the option 'Gifted' when the model contains a 'Gifted'" in {
      val model = HowBecameOwnerModel("Gifted")
      val form = howBecameOwnerForm.fill(model)
      form.data.get("gainedBy") shouldBe Some("Gifted")
    }

    "create a map with the option 'Inherited' when the model contains a 'Inherited'" in {
      val model = HowBecameOwnerModel("Inherited")
      val form = howBecameOwnerForm.fill(model)
      form.data.get("gainedBy") shouldBe Some("Inherited")
    }
  }

  "Creating the form from a valid map" should {

    "create a model containing 'Bought' when provided with a map containing 'Bought'" in {
      val map = Map(("gainedBy", "Bought"))
      val form = howBecameOwnerForm.bind(map)
      form.value shouldBe Some(HowBecameOwnerModel("Bought"))
    }

    "create a model containing 'Gifted' when provided with a map containing 'Gifted'" in {
      val map = Map(("gainedBy", "Gifted"))
      val form = howBecameOwnerForm.bind(map)
      form.value shouldBe Some(HowBecameOwnerModel("Gifted"))
    }

    "create a model containing 'Inherited' when provided with a map containing 'Inherited'" in {
      val map = Map(("gainedBy", "Inherited"))
      val form = howBecameOwnerForm.bind(map)
      form.value shouldBe Some(HowBecameOwnerModel("Inherited"))
    }
  }

  "Creating the form from an invalid map" when {

    "no data is provided" should {
      lazy val map = Map(("gainedBy", ""))
      lazy val form = howBecameOwnerForm.bind(map)

      "produce a form with errors" in {
        form.hasErrors shouldBe true
      }

      "contain only one error" in {
        form.errors.length shouldBe 1
      }

      s"contain the error message ${messages.errorMandatory}" in{
        form.errors.head.message shouldBe messages.errorMandatory
      }
    }

    "incorrect data is provided" should {
      lazy val map = Map(("gainedBy", "badData"))
      lazy val form = howBecameOwnerForm.bind(map)

      "produce a form with errors" in {
        form.hasErrors shouldBe true
      }

      "contain only one error" in {
        form.errors.length shouldBe 1
      }

      s"contain the error message ${messages.errorMandatory}" in{
        form.errors.head.message shouldBe messages.errorMandatory
      }
    }
  }
}
