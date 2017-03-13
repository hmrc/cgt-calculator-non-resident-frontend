/*
 * Copyright 2017 HM Revenue & Customs
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
import models.AllowableLossesModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object AllowableLossesForm {

  def validate(data: AllowableLossesModel): Boolean = {
    data.isClaimingAllowableLosses match {
      case "Yes" => data.allowableLossesAmt.isDefined
      case "No" => true
      }
    }

  def validateMinimum(data: AllowableLossesModel): Boolean = {
    data.isClaimingAllowableLosses match {
      case "Yes" => isPositive(data.allowableLossesAmt.getOrElse(0))
      case "No" => true
    }
  }

  def validateTwoDec(data: AllowableLossesModel): Boolean = {
    data.isClaimingAllowableLosses match {
      case "Yes" => decimalPlacesCheck(data.allowableLossesAmt.getOrElse(0))
      case "No" => true
    }
  }

  def validateMax(data: AllowableLossesModel): Boolean = {
    data.isClaimingAllowableLosses match {
      case "Yes" => maxCheck(data.allowableLossesAmt.getOrElse(0))
      case "No" => true
    }
  }

  val allowableLossesForm = Form(
    mapping(
      "isClaimingAllowableLosses" -> text
        .verifying(Messages("error.required"), mandatoryCheck)
        .verifying(Messages("error.required"), yesNoCheck),
      "allowableLossesAmt" -> optional(bigDecimal)
    )(AllowableLossesModel.apply)(AllowableLossesModel.unapply)
      .verifying(Messages("calc.allowableLosses.errorQuestion"),
        allowableLossesForm => validate(AllowableLossesModel(allowableLossesForm.isClaimingAllowableLosses, allowableLossesForm.allowableLossesAmt)))
      .verifying(Messages("calc.allowableLosses.errorMinimum"),
        allowableLossesForm => validateMinimum(AllowableLossesModel(allowableLossesForm.isClaimingAllowableLosses, allowableLossesForm.allowableLossesAmt)))
      .verifying(Messages("calc.allowableLosses.errorDecimal"),
        allowableLossesForm => validateTwoDec(AllowableLossesModel(allowableLossesForm.isClaimingAllowableLosses, allowableLossesForm.allowableLossesAmt)))
      .verifying(Messages("calc.common.error.maxNumericExceeded") + MoneyPounds(Constants.maxNumeric, 0).quantity + " " + Messages("calc.common.error.maxNumericExceeded.OrLess"),
        allowableLossesForm => validateMax(AllowableLossesModel(allowableLossesForm.isClaimingAllowableLosses, allowableLossesForm.allowableLossesAmt)))
  )
}
