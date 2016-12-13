/*
 * Copyright 2016 HM Revenue & Customs
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

package forms.nonresident

import common.Constants
import common.Validation._
import models.nonresident.RebasedCostsModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds
import common.Transformers._

object RebasedCostsForm {

  def verifyAmountSupplied(data: RebasedCostsModel): Boolean = {
    data.hasRebasedCosts match {
      case "Yes" => data.rebasedCosts.isDefined
      case "No" => true
    }
  }

  def verifyPositive(data: RebasedCostsModel): Boolean = {
    data.hasRebasedCosts match {
      case "Yes" => isPositive(data.rebasedCosts.getOrElse(0))
      case "No" => true
    }
  }

  def verifyTwoDecimalPlaces(data: RebasedCostsModel): Boolean = {
    data.hasRebasedCosts match {
      case "Yes" => decimalPlacesCheck(data.rebasedCosts.getOrElse(0))
      case "No" => true
    }
  }

  def validateMax(data: RebasedCostsModel): Boolean = {
    data.hasRebasedCosts match {
      case "Yes" => maxCheck(data.rebasedCosts.getOrElse(0))
      case "No" => true
    }
  }

  val rebasedCostsForm = Form(
    mapping(
      "hasRebasedCosts" -> text
        .verifying(Messages("calc.common.error.fieldRequired"), mandatoryCheck)
        .verifying(Messages("calc.common.error.fieldRequired"), yesNoCheck),
      "rebasedCosts" -> text
        .transform(stringToOptionalBigDecimal, optionalBigDecimalToString)
    )(RebasedCostsModel.apply)(RebasedCostsModel.unapply)
      .verifying(Messages("calc.rebasedCosts.error.no.value.supplied"),
        rebasedCostsForm => verifyAmountSupplied(RebasedCostsModel(rebasedCostsForm.hasRebasedCosts, rebasedCostsForm.rebasedCosts)))
      .verifying(Messages("calc.rebasedCosts.errorNegative"),
        rebasedCostsForm => verifyPositive(rebasedCostsForm))
      .verifying(Messages("calc.rebasedCosts.errorDecimalPlaces"),
        rebasedCostsForm => verifyTwoDecimalPlaces(rebasedCostsForm))
      .verifying(Messages("calc.common.error.maxNumericExceeded") + MoneyPounds(Constants.maxNumeric, 0).quantity + " " + Messages("calc.common.error.maxNumericExceeded.OrLess"),
        rebasedValueForm => validateMax(rebasedValueForm))
  )
}
