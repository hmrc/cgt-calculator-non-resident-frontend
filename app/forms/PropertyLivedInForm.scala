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

import common.Formatters.text
import common.Transformers._
import common.Validation._
import models.PropertyLivedInModel
import play.api.data.Forms._
import play.api.data._

object PropertyLivedInForm {

  val propertyLivedInForm: Form[PropertyLivedInModel] =
    Form(
      mapping(
        "propertyLivedIn" -> text("calc.propertyLivedIn.errors.required")
          .verifying("calc.propertyLivedIn.errors.required", mandatoryCheck)
          .verifying("calc.propertyLivedIn.errors.required", yesNoCheck)
          .transform(stringToBoolean, booleanToString)
      )(PropertyLivedInModel.apply)(o=>Some(o.propertyLivedIn))
    )
}
