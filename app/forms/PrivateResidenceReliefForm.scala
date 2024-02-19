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

import common.Validation._
import common.Transformers._
import models.PrivateResidenceReliefModel
import play.api.data.Forms._
import play.api.data._
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object PrivateResidenceReliefForm {

  val isClaimingPRR = "isClaimingPRR" -> common.Formatters.text("calc.privateResidenceRelief.errors.required")
    .verifying("calc.privateResidenceRelief.errors.required", mandatoryCheck)
    .verifying("calc.privateResidenceRelief.errors.required", yesNoCheck)

  val daysClaimed = "daysClaimed" -> mandatoryIf(
    isEqual("isClaimingPRR", "Yes"),
    common.Formatters.text("calc.privateResidenceRelief.error.noValueProvided")
      .verifying("error.number", bigDecimalCheck)
      .transform(stringToBigDecimal, bigDecimalToString)
      .verifying(
        stopOnFirstFail(
          negativeConstraint("calc.privateResidenceRelief.error.errorNegative"),
          decimalPlaceConstraint("calc.privateResidenceRelief.error.errorDecimalPlaces", 1),
          maxMonetaryValueConstraint(errMsgKey = "calc.privateResidenceRelief.error.maxNumericExceeded")
        )
      )
  )

  val daysClaimedAfter = "daysClaimedAfter" -> mandatoryIf(
    isEqual("isClaimingPRR", "Yes"),
    common.Formatters.text("calc.privateResidenceRelief.error.noValueProvided")
      .verifying("error.number", bigDecimalCheck)
      .transform(stringToBigDecimal, bigDecimalToString)
      .verifying(
        stopOnFirstFail(
          negativeConstraint("calc.privateResidenceRelief.error.errorNegative"),
          decimalPlaceConstraint("calc.privateResidenceRelief.error.errorDecimalPlaces", 1),
          maxMonetaryValueConstraint(errMsgKey = "calc.privateResidenceRelief.error.maxNumericExceeded")
        )
      )
  )

  def emptyForm(fieldName: String) = {
    fieldName -> optional(text)
      .transform(optionalStringToOptionalBigDecimal, optionalBigDecimalToOptionalString)
  }

  def privateResidenceReliefForm (showBefore: Boolean, showAfter: Boolean): Form[PrivateResidenceReliefModel] =
    (showBefore, showAfter) match {
      case (true, true) => Form(
        mapping(isClaimingPRR, daysClaimed, daysClaimedAfter)
        (PrivateResidenceReliefModel.apply)(PrivateResidenceReliefModel.unapply)
      )
      case (true, false) => Form(
        mapping(isClaimingPRR, daysClaimed, emptyForm("daysClaimedAfter"))
        (PrivateResidenceReliefModel.apply)(PrivateResidenceReliefModel.unapply)
      )
      case (false, true) => Form(
        mapping(isClaimingPRR, emptyForm("daysClaimed"), daysClaimedAfter)
        (PrivateResidenceReliefModel.apply)(PrivateResidenceReliefModel.unapply)
      )
      case _ => Form(
        mapping(isClaimingPRR, emptyForm("daysClaimed"), emptyForm("daysClaimedAfter"))
        (PrivateResidenceReliefModel.apply)(PrivateResidenceReliefModel.unapply)
      )
    }
}
