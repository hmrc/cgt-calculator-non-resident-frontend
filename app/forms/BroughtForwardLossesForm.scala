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

import common.Constants
import common.Transformers._
import common.Validation._
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

  val broughtForwardLossesForm: Form[BroughtForwardLossesModel] = Form(
    mapping(
      "isClaiming" -> common.Formatters.text("calc.broughtForwardLosses.errors.required")
        .verifying("calc.broughtForwardLosses.errors.required", mandatoryCheck)
        .verifying("calc.broughtForwardLosses.errors.required", yesNoCheck)
        .transform(stringToBoolean, booleanToString),
      "broughtForwardLoss" -> mandatoryIf(
        isEqual("isClaiming", "Yes"),
        common.Formatters.text("calc.broughtForwardLosses.error.required")
          .transform(stripCurrencyCharacters, stripCurrencyCharacters)
          .verifying("calc.broughtForwardLosses.error.required", mandatoryCheck)
          .verifying("calc.broughtForwardLosses.error.invalid", bigDecimalCheck)
          .transform(stringToBigDecimal, bigDecimalToString).verifying()
          .verifying("calc.broughtForwardLosses.errorDecimal", decimalPlacesCheck)
          .verifying("calc.broughtForwardLosses.errorNegative", isPositive)
          .verifying(maxMonetaryValueConstraint(Constants.maxNumeric, "calc.broughtForwardLosses.error.tooHigh"))
      )
    )(BroughtForwardLossesModel.apply)(o=>Some(o.isClaiming,o.broughtForwardLoss))

  )
}
