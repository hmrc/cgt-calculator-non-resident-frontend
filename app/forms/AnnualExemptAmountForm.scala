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
import models.AnnualExemptAmountModel
import play.api.data.Forms._
import play.api.data._

object AnnualExemptAmountForm {

  def annualExemptAmountForm(maxAEA: BigDecimal = BigDecimal(0)): Form[AnnualExemptAmountModel] = Form(
    mapping(
      "annualExemptAmount" -> text
        .transform(stripCurrencyCharacters, stripCurrencyCharacters)
        .verifying("calc.annualExemptAmount.error.required", mandatoryCheck)
        .verifying("calc.annualExemptAmount.errorReal", bigDecimalCheck)
        .transform(stringToBigDecimal, bigDecimalToString)
        .verifying(maxMonetaryValueConstraint(maxAEA, "calc.annualExemptAmount.errorMax"))
        .verifying("calc.annualExemptAmount.errorNegative", annualExemptAmount => isPositive(annualExemptAmount))
        .verifying("calc.annualExemptAmount.errorDecimalPlaces", annualExemptAmount => decimalPlacesCheck(annualExemptAmount))
    )(AnnualExemptAmountModel.apply)(o=>Some(o.annualExemptAmount))
  )
}
