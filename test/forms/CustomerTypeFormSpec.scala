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

import assets.MessageLookup.{NonResident => messages}
import models.CustomerTypeModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CustomerTypeFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model with individual" should {
      val model = CustomerTypeModel("individual")
      lazy val form = customerTypeForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("customerType" -> "individual")
      }
    }

    "passing in a valid map with individual" should {
      val map = Map("customerType" -> "individual")
      lazy val form = customerTypeForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(CustomerTypeModel("individual"))
      }
    }

    "passing in a valid map with trustee" should {
      val map = Map("customerType" -> "trustee")
      lazy val form = customerTypeForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(CustomerTypeModel("trustee"))
      }
    }

    "passing in a valid map with personalRep" should {
      val map = Map("customerType" -> "personalRep")
      lazy val form = customerTypeForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(CustomerTypeModel("personalRep"))
      }
    }

    "passing in an invalid map with asdf" should {
      val map = Map("customerType" -> "asdf")
      lazy val form = customerTypeForm.bind(map)

      "return a valid form with a single error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.CustomerType.errorInvalid}" in {
        form.error("customerType").get.message shouldBe messages.CustomerType.errorInvalid
      }
    }

    "passing in an invalid map with " should {
      val map = Map("customerType" -> "")
      lazy val form = customerTypeForm.bind(map)

      "return a valid form with a single error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.CustomerType.errorInvalid}" in {
        form.error("customerType").get.message shouldBe messages.CustomerType.errorInvalid
      }
    }
  }
}
