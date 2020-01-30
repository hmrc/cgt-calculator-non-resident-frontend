/*
 * Copyright 2020 HM Revenue & Customs
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
import models.ImprovementsModel
import play.api.data.Forms._
import play.api.data._
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object ImprovementsForm {

  private def verifyAmountSupplied(data: ImprovementsModel, showHiddenQuestion: Boolean): Boolean = {
    data.isClaimingImprovements match {
      case "Yes" if showHiddenQuestion => data.improvementsAmt.isDefined || data.improvementsAmtAfter.isDefined
      case "Yes" => data.improvementsAmt.isDefined
      case _ => true
    }
  }

  private def verifyPositive(data: ImprovementsModel, showHiddenQuestion: Boolean): Boolean = {
    data.isClaimingImprovements match {
      case "Yes" if showHiddenQuestion => isPositive(data.improvementsAmt.getOrElse(0)) && isPositive(data.improvementsAmtAfter.getOrElse(0))
      case "Yes" => isPositive(data.improvementsAmt.getOrElse(0))
      case "No" => true
    }
  }

  private def verifyTwoDecimalPlaces(data: ImprovementsModel, showHiddenQuestion: Boolean): Boolean = {
    data.isClaimingImprovements match {
      case "Yes" if showHiddenQuestion => decimalPlacesCheck(data.improvementsAmt.getOrElse(0)) && decimalPlacesCheck(data.improvementsAmtAfter.getOrElse(0))
      case "Yes" => decimalPlacesCheck(data.improvementsAmt.getOrElse(0))
      case "No" => true
    }
  }

  private def extractImprovementsAmount(model: ImprovementsModel): Option[BigDecimal] = {
    if(model.isClaimingImprovements == "Yes") model.improvementsAmt else None
  }

  private def extractImprovementsAfterAmount(model: ImprovementsModel, showHiddenQuestion: Boolean): Option[BigDecimal] = {
    if(showHiddenQuestion && model.isClaimingImprovements == "Yes") model.improvementsAmtAfter else None
  }

  def improvementsForm(showHiddenQuestion: Boolean): Form[ImprovementsModel] = Form(
    mapping(
      "isClaimingImprovements" -> text
      .verifying("calc.common.error.fieldRequired", mandatoryCheck)
      .verifying("calc.common.error.fieldRequired", yesNoCheck),
      "improvementsAmt" -> optional(text)
        .transform(optionalStringToOptionalBigDecimal, optionalBigDecimalToOptionalString),
      "improvementsAmtAfter" -> optional(text)
        .transform(optionalStringToOptionalBigDecimal, optionalBigDecimalToOptionalString)
    )(ImprovementsModel.apply)(ImprovementsModel.unapply)
      .verifying("calc.improvements.error.no.value.supplied",
        improvementsForm => verifyAmountSupplied(improvementsForm, showHiddenQuestion))
      .verifying("calc.improvements.errorNegative",
        improvementsForm => verifyPositive(improvementsForm, showHiddenQuestion))
      .verifying("calc.improvements.errorDecimalPlaces",
        improvementsForm => verifyTwoDecimalPlaces(improvementsForm, showHiddenQuestion))
      .verifying(maxMonetaryValueConstraint[ImprovementsModel](Constants.maxNumeric, extractImprovementsAmount))
      .verifying(maxMonetaryValueConstraint[ImprovementsModel](
        Constants.maxNumeric, model => extractImprovementsAfterAmount(model, showHiddenQuestion)
      ))
  )
}
