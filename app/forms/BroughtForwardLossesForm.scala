/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.BroughtForwardLossesForm.verifyMandatory
import models.BroughtForwardLossesModel
import play.api.data.Forms._
import play.api.data._
import uk.gov.voa.play.form.ConditionalMappings._

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

  private def getLossesAmount(model: BroughtForwardLossesModel): Option[BigDecimal] = {
    model match {
      case BroughtForwardLossesModel(true, losses) => losses
      case _ => None
    }
  }

  val broughtForwardLossesForm = Form(
    mapping(
      "isClaiming" -> common.Formatters.text("calc.broughtForwardLosses.errors.required")
        .verifying("calc.broughtForwardLosses.errors.required", mandatoryCheck)
        .verifying("calc.broughtForwardLosses.errors.required", yesNoCheck)
        .transform(stringToBoolean, booleanToString),
      "broughtForwardLoss" -> mandatoryIf(
        isEqual("isClaiming", "Yes"),
        common.Formatters.text("error.real")
          .transform(stripCurrencyCharacters, stripCurrencyCharacters)
          .verifying("error.real", bigDecimalCheck)
          .transform(stringToBigDecimal, bigDecimalToString)
      )
    )(BroughtForwardLossesModel.apply)(BroughtForwardLossesModel.unapply)
      .verifying("error.real", verifyMandatory)
      .verifying("calc.broughtForwardLosses.errorDecimal", verifyDecimal)
      .verifying("calc.broughtForwardLosses.errorNegative", verifyPositive)
      .verifying(maxMonetaryValueConstraint[BroughtForwardLossesModel](Constants.maxNumeric, getLossesAmount))
  )
}
