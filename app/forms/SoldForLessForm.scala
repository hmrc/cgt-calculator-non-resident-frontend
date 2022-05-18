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

import common.Transformers._
import common.Validation._
import models.SoldForLessModel
import play.api.data.Form
import play.api.data.Forms._
import common.Formatters.text

object SoldForLessForm {

  val soldForLessForm: Form[SoldForLessModel] = Form(
    mapping(
      "soldForLess" -> text("calc.nonResident.soldForLess.errors.required")
        .verifying("calc.nonResident.soldForLess.errors.required", mandatoryCheck)
        .verifying("calc.nonResident.soldForLess.errors.required", yesNoCheck)
        .transform[Boolean](stringToBoolean, booleanToString)
    )(SoldForLessModel.apply)(SoldForLessModel.unapply)
  )
}
