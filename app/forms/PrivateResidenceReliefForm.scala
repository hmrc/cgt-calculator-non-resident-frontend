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

import common.Transformers._
import common.Validation._
import models.{ClaimingPrrModel, PrivateResidenceReliefModel}
import play.api.data.Forms._
import play.api.data._
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object PrivateResidenceReliefForm {

  private val isClaimingPRR = "isClaimingPRR" -> common.Formatters.text("calc.privateResidenceRelief.errors.required")
    .verifying("calc.privateResidenceRelief.errors.required", mandatoryCheck)
    .verifying("calc.privateResidenceRelief.errors.required", yesNoCheck)

  private val daysClaimed = "daysClaimed" -> mandatoryIf(
    isEqual("isClaimingPRR", "Yes"),
    common.Formatters.text("calc.privateResidenceReliefValue.error.noValueProvided")
      .transform(stripCurrencyCharacters, stripCurrencyCharacters)
      .verifying("calc.privateResidenceReliefValue.error.required", mandatoryCheck)
      .verifying("calc.privateResidenceReliefValue.error.number", bigDecimalCheck)
      .transform(stringToBigDecimal, bigDecimalToString)
      .verifying("calc.privateResidenceReliefValue.error.errorNegative", isPositive)
      .verifying("calc.privateResidenceReliefValue.error.errorDecimalPlaces", decimalPlacesCheck)
      .verifying(maxMonetaryValueConstraint(errMsgKey = "calc.privateResidenceRelief.error.maxNumericExceeded"))
  )

  def isClaimingPrrForm: Form[ClaimingPrrModel] =
      Form(mapping(isClaimingPRR)(ClaimingPrrModel.apply)(ClaimingPrrModel.unapply))

  def privateResidenceReliefForm: Form[PrivateResidenceReliefModel] = Form(mapping(isClaimingPRR, daysClaimed)(PrivateResidenceReliefModel.apply)(PrivateResidenceReliefModel.unapply))
}
