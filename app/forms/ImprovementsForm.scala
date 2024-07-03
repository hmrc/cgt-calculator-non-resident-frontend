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
import models.ImprovementsModel
import play.api.data.Forms._
import play.api.data._

object ImprovementsForm {

  def improvementsForm(isBeforeTaxStarts: Boolean): Form[ImprovementsModel] =
    if (isBeforeTaxStarts) {
      Form(mapping(
          "improvementsAmt" ->
            common.Formatters.text("calc.improvements.before.error.required")
              .transform(stripCurrencyCharacters, stripCurrencyCharacters)
              .verifying("calc.improvements.before.error.invalid", bigDecimalCheck)
              .transform(stringToBigDecimal, bigDecimalToString)
              .verifying(
                stopOnFirstFail(
                  negativeConstraint("calc.improvements.before.error.tooLow"),
                  decimalPlaceConstraint("calc.improvements.before.error.decimalPlaces"),
                  maxMonetaryValueConstraint(errMsgKey = "calc.improvements.before.error.tooHigh")
                )
          ),
          "improvementsAmtAfter" ->
            optional(text)
              .verifying("calc.improvements.after.error.required", optionalCannotBeEmpty)
              .transform(stripOptionalCurrencyCharacters, stripOptionalCurrencyCharacters)
              .verifying("calc.improvements.after.error.invalid", optionalBigDecimalCheck)
              .transform(optionalStringToOptionalBigDecimal, optionalBigDecimalToOptionalString)
              .verifying(
                stopOnFirstFail(
                  optionalNegativeConstraint("calc.improvements.after.error.tooLow"),
                  optionalDecimalPlaceConstraint("calc.improvements.after.error.decimalPlaces"),
                  optionalMaxMonetaryValueConstraint(errMsgKey = "calc.improvements.after.error.tooHigh")
                )

          )
        )(ImprovementsModel.apply)(ImprovementsModel.unapply))
    } else {
      Form(mapping(
          "improvementsAmt" ->
            common.Formatters.text("calc.improvements.error.required")
              .transform(stripCurrencyCharacters, stripCurrencyCharacters)
              .verifying("calc.improvements.error.invalid", bigDecimalCheck)
              .transform(stringToBigDecimal, bigDecimalToString)
              .verifying(
                stopOnFirstFail(
                  negativeConstraint("calc.improvements.errorNegative"),
                  decimalPlaceConstraint("calc.improvements.errorDecimalPlaces"),
                  maxMonetaryValueConstraint()
                )
          ),
        "improvementsAmtAfter" -> optional(text).transform(optionalStringToOptionalBigDecimal, optionalBigDecimalToOptionalString)
        )(ImprovementsModel.apply)(ImprovementsModel.unapply))
    }
}
