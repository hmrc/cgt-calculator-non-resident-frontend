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

import assets.KeyLookup.{NonResident => messages}
import common.nonresident.{Flat, TimeApportioned}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.CalculationElectionForm._
import models.CalculationElectionModel

class CalculationElectionFormSpec extends CommonPlaySpec with WithCommonFakeApplication {

  "Creating a form with a valid model" should {

      lazy val model = CalculationElectionModel(Flat)
      lazy val form = calculationElectionForm.fill(model)

    "return a form with 0 errors" in {
      form.errors.size shouldBe 0
    }

    "return a form with the data specified in the model" in {
      form.value shouldBe Some(model)
    }
  }

}
