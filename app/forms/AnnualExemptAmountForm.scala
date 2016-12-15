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

package forms

import common.Transformers._
import common.Validation._
import models.AnnualExemptAmountModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object AnnualExemptAmountForm {

  def errorMaxMessage(maxAEA: BigDecimal): String =
    Messages("calc.annualExemptAmount.errorMax") + MoneyPounds(maxAEA, 0).quantity + " " + Messages("calc.annualExemptAmount.errorMaxEnd")

  def annualExemptAmountForm(maxAEA: BigDecimal = BigDecimal(0)): Form[AnnualExemptAmountModel] = Form(
    mapping(
      "annualExemptAmount" -> text
        .verifying(Messages("error.real"), mandatoryCheck)
        .verifying(Messages("error.real"), bigDecimalCheck)
        .transform(stringToBigDecimal, bigDecimalToString)
        .verifying(errorMaxMessage(maxAEA), _ <= maxAEA)
        .verifying(Messages("calc.annualExemptAmount.errorNegative"), annualExemptAmount => isPositive(annualExemptAmount))
        .verifying(Messages("calc.annualExemptAmount.errorDecimalPlaces"), annualExemptAmount => decimalPlacesCheck(annualExemptAmount))
    )(AnnualExemptAmountModel.apply)(AnnualExemptAmountModel.unapply)
  )
}
