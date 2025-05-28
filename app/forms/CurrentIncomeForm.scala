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
import models.CurrentIncomeModel
import play.api.data.Forms._
import play.api.data._

object CurrentIncomeForm {

  val currentIncomeForm: Form[CurrentIncomeModel] = Form(
    mapping(
      "currentIncome" -> text
        .transform(stripCurrencyCharacters, stripCurrencyCharacters)
        .verifying("calc.currentIncome.error.required", mandatoryCheck)
        .verifying("calc.currentIncome.errorReal", bigDecimalCheck)
        .transform(stringToBigDecimal, bigDecimalToString)
        .verifying("calc.currentIncome.errorNegative", isPositive)
        .verifying("calc.currentIncome.errorDecimalPlaces", decimalPlacesCheck)
        .verifying("calc.currentIncome.errorMax", maxCheck)
    )(CurrentIncomeModel.apply)(o=>Some(o.currentIncome))
  )
}
