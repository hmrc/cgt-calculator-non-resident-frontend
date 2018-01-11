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
import common.Transformers._
import common.Validation._
import models.BroughtForwardLossesModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object BroughtForwardLossesForm {

  val verifyMandatory: BroughtForwardLossesModel => Boolean = {
    case BroughtForwardLossesModel(true, value) => value.isDefined
    case _ => true
  }

  val verifyDecimal: BroughtForwardLossesModel => Boolean = {
    case BroughtForwardLossesModel(true, Some(value)) => decimalPlacesCheck(value)
    case _ => true
  }

  val verifyPositive: BroughtForwardLossesModel => Boolean = {
    case BroughtForwardLossesModel(true, Some(value)) => isPositive(value)
    case _ => true
  }

  val verifyMaximum: BroughtForwardLossesModel => Boolean = {
    case BroughtForwardLossesModel(true, Some(value)) => maxCheck(value)
    case _ => true
  }

  val broughtForwardLossesForm = Form(
    mapping(
      "isClaiming" -> text
        .verifying(Messages("calc.common.error.fieldRequired"), mandatoryCheck)
        .verifying(Messages("calc.common.error.fieldRequired"), yesNoCheck)
        .transform(stringToBoolean, booleanToString),
      "broughtForwardLoss" -> text
        .transform(stringToOptionalBigDecimal, optionalBigDecimalToString)
    )(BroughtForwardLossesModel.apply)(BroughtForwardLossesModel.unapply)
      .verifying(Messages("error.real"), verifyMandatory)
      .verifying(Messages("calc.broughtForwardLosses.errorDecimal"), verifyDecimal)
      .verifying(Messages("calc.broughtForwardLosses.errorNegative"), verifyPositive)
      .verifying(Messages("calc.common.error.maxNumericExceeded") + MoneyPounds(Constants.maxNumeric, 0).quantity + " " +
        Messages("calc.common.error.maxNumericExceeded.OrLess"), verifyMaximum)
  )
}
