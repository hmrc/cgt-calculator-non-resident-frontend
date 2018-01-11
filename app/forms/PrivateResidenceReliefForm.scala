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

import java.text.NumberFormat

import common.Constants
import common.Validation._
import common.Transformers._
import models.PrivateResidenceReliefModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object PrivateResidenceReliefForm {

  val formatter: NumberFormat = java.text.NumberFormat.getIntegerInstance

  def verifyAmountSupplied(data: PrivateResidenceReliefModel, showBefore: Boolean, showAfter: Boolean): Boolean = {
    data.isClaimingPRR match {
      case "Yes" if showBefore && showAfter => data.daysClaimed.isDefined && data.daysClaimedAfter.isDefined
      case "Yes" if showBefore ^ showAfter => data.daysClaimed.isDefined || data.daysClaimedAfter.isDefined
      case _ => true
    }
  }

  def verifyPositive (data: PrivateResidenceReliefModel, showBefore: Boolean, showAfter: Boolean): Boolean = {
    data.isClaimingPRR match {
      case "Yes" if showBefore || showAfter => isPositive(data.daysClaimed.getOrElse(0)) && isPositive(data.daysClaimedAfter.getOrElse(0))
      case _ => true
    }
  }

  def verifyNoDecimalPlaces (data: PrivateResidenceReliefModel, showBefore: Boolean, showAfter: Boolean): Boolean = {
    data.isClaimingPRR match {
      case "Yes" if showBefore || showAfter =>
        decimalPlacesCheckNoDecimal(data.daysClaimed.getOrElse(0)) &&
        decimalPlacesCheckNoDecimal(data.daysClaimedAfter.getOrElse(0))
      case _ => true
    }
  }

  def validateMax (data: PrivateResidenceReliefModel, showBefore: Boolean, showAfter: Boolean): Boolean = {
    data.isClaimingPRR match {
      case "Yes" if showBefore || showAfter => maxCheck(data.daysClaimed.getOrElse(0)) && maxCheck(data.daysClaimedAfter.getOrElse(0))
      case _ => true
    }
  }

  def privateResidenceReliefForm (showBefore: Boolean, showAfter: Boolean): Form[PrivateResidenceReliefModel] = Form(
    mapping(
      "isClaimingPRR" -> text
        .verifying(Messages("calc.common.error.fieldRequired"), mandatoryCheck)
        .verifying(Messages("calc.common.error.fieldRequired"), yesNoCheck),
      "daysClaimed" -> optional(text)
        .transform(optionalStringToOptionalBigDecimal, optionalBigDecimalToOptionalString),
      "daysClaimedAfter" -> optional(text)
        .transform(optionalStringToOptionalBigDecimal, optionalBigDecimalToOptionalString)
    )(PrivateResidenceReliefModel.apply)(PrivateResidenceReliefModel.unapply)
      .verifying(Messages("calc.privateResidenceRelief.error.noValueProvided"), form => verifyAmountSupplied(form, showBefore, showAfter))
      .verifying(Messages("calc.privateResidenceRelief.error.errorNegative"), form => verifyPositive(form, showBefore, showAfter))
      .verifying(Messages("calc.privateResidenceRelief.error.errorDecimalPlaces"), form => verifyNoDecimalPlaces(form, showBefore, showAfter))
      .verifying(Messages("calc.privateResidenceRelief.error.maxNumericExceeded") + " " + formatter.format(Constants.maxNumeric) + " " + Messages("calc.privateResidenceRelief.error.maxNumericExceeded.OrLess"),
        form => validateMax(form, showBefore, showAfter)
      )
  )
}
