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
import models.CostsAtLegislationStartModel
import play.api.data.Forms._
import play.api.data._
import uk.gov.hmrc.play.views.helpers.MoneyPounds
import common.Transformers._

object CostsAtLegislationStartForm {

  def verifyAmountSupplied(data: CostsAtLegislationStartModel): Boolean = {
    data.hasCosts match {
      case "Yes" => data.costs.isDefined
      case "No" => true
    }
  }

  def verifyPositive(data: CostsAtLegislationStartModel): Boolean = {
    data.hasCosts match {
      case "Yes" => isPositive(data.costs.getOrElse(0))
      case "No" => true
    }
  }

  def verifyTwoDecimalPlaces(data: CostsAtLegislationStartModel): Boolean = {
    data.hasCosts match {
      case "Yes" => decimalPlacesCheck(data.costs.getOrElse(0))
      case "No" => true
    }
  }

  def validateMax(data: CostsAtLegislationStartModel): Boolean = {
    data.hasCosts match {
      case "Yes" => maxCheck(data.costs.getOrElse(0))
      case "No" => true
    }
  }

  private lazy val greaterThanMaxMessage = "calc.common.error.maxNumericExceeded" +
    MoneyPounds(Constants.maxNumeric, 0).quantity +
    " " +
    "calc.common.error.maxNumericExceeded.OrLess"

  val costsAtLegislationStartForm = Form(
    mapping(
      "hasCosts" -> text
        .verifying("calc.common.error.fieldRequired", mandatoryCheck)
        .verifying("calc.common.error.fieldRequired", yesNoCheck),
      "costs" -> text
        .transform(stringToOptionalBigDecimal, optionalBigDecimalToString)
    )(CostsAtLegislationStartModel.apply)(CostsAtLegislationStartModel.unapply)
      .verifying("calc.costsAtLegislationStart.error.no.value.supplied",
        form => verifyAmountSupplied(CostsAtLegislationStartModel(form.hasCosts, form.costs)))
      .verifying("calc.costsAtLegislationStart.errorNegative",
        form => verifyPositive(form))
      .verifying("calc.costsAtLegislationStart.errorDecimalPlaces",
        form => verifyTwoDecimalPlaces(form))
      .verifying(greaterThanMaxMessage,
        form => validateMax(form))
  )
}
