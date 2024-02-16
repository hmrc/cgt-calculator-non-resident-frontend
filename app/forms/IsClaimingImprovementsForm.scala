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
import play.api.data.Form
import play.api.data.Forms._

object IsClaimingImprovementsForm {

  val form: Form[IsClaimingImprovementsModel] = Form(
    mapping(
      "isClaimingImprovements" -> text("calc.improvements.errors.required")
        .verifying("calc.improvements.errors.required", mandatoryCheck)
        .verifying("calc.calc.improvements.errors.required", yesNoCheck)
        .transform[Boolean](stringToBoolean, booleanToString)
    )(IsClaimingImprovementsModel.apply)(IsClaimingImprovementsModel.unapply)
  )
}
