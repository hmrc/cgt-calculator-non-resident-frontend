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

import common.Transformers.{booleanToString, stringToBoolean}
import common.Validation.{mandatoryCheck, yesNoCheck}
import models.ClaimingReliefsModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object ClaimingReliefsForm {

  val claimingReliefsForm: Form[ClaimingReliefsModel] = Form(
    mapping(
      "isClaimingReliefs" -> text
        .verifying("calc.claimingReliefs.errorMandatory", mandatoryCheck)
        .verifying("calc.claimingReliefs.errorMandatory", yesNoCheck)
        .transform(stringToBoolean, booleanToString)
    )(ClaimingReliefsModel.apply)(ClaimingReliefsModel.unapply)
  )
}
