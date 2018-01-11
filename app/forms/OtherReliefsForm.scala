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

import common.Constants
import common.Validation._
import common.Transformers._
import models.OtherReliefsModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object OtherReliefsForm {

  val otherReliefsForm: Form[OtherReliefsModel] =
    Form(
      mapping(
        "otherReliefs" -> text
          .verifying(Messages("error.real"), mandatoryCheck)
          .verifying(Messages("error.real"), bigDecimalCheck)
          .transform(stringToBigDecimal, bigDecimalToString)
          .verifying(Messages("calc.otherReliefs.errorMinimum"), isPositive)
          .verifying(Messages("calc.otherReliefs.errorDecimal"), decimalPlacesCheck)
          .verifying(Messages("calc.common.error.maxNumericExceeded") + MoneyPounds(Constants.maxNumeric, 0).quantity +
            " " + Messages("calc.common.error.maxNumericExceeded.OrLess"),
            maxCheck)
      )(OtherReliefsModel.apply)(OtherReliefsModel.unapply)
    )
}
