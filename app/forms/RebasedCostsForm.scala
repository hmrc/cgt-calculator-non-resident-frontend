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
import models.RebasedCostsModel
import play.api.data.Forms._
import play.api.data._
import uk.gov.voa.play.form.ConditionalMappings._

object RebasedCostsForm {

  val rebasedCostsForm: Form[RebasedCostsModel] = Form(
    mapping(
      "hasRebasedCosts" -> common.Formatters.text("calc.rebasedCosts.errors.required")
        .verifying("calc.rebasedCosts.errors.required", mandatoryCheck)
        .verifying("calc.rebasedCosts.errors.required", yesNoCheck),
      "rebasedCosts" -> mandatoryIf(
        isEqual("hasRebasedCosts", "Yes"),
        common.Formatters.text("calc.rebasedCosts.error.required")
          .transform(stripCurrencyCharacters, stripCurrencyCharacters)
          .verifying("calc.rebasedCosts.error.invalid", bigDecimalCheck)
          .transform(stringToBigDecimal, bigDecimalToString)
          .verifying(
            stopOnFirstFail(
              negativeConstraint("calc.rebasedCosts.errorNegative"),
              decimalPlaceConstraint("calc.rebasedCosts.errorDecimalPlaces"),
              maxMonetaryValueConstraint(errMsgKey = "calc.rebasedCosts.error.tooHigh")
            )
          )
      )
    )(RebasedCostsModel.apply)(RebasedCostsModel.unapply)
  )
}
