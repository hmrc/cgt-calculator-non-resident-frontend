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
import models.CostsAtLegislationStartModel
import play.api.data.Forms._
import play.api.data._
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object CostsAtLegislationStartForm {

  val costsAtLegislationStartForm: Form[CostsAtLegislationStartModel] = Form(
    mapping(
      "hasCosts" -> common.Formatters.text("calc.costsAtLegislationStart.errors.required")
        .verifying("calc.costsAtLegislationStart.errors.required", mandatoryCheck)
        .verifying("calc.costsAtLegislationStart.errors.required", yesNoCheck),
      "costs" -> mandatoryIf(
        isEqual("hasCosts", "Yes"),
        common.Formatters.text("calc.costsAtLegislationStart.error.required")
          .transform(stripCurrencyCharacters, stripCurrencyCharacters)
          .verifying("calc.costsAtLegislationStart.error.invalid", bigDecimalCheck)
          .transform(stringToBigDecimal, bigDecimalToString)
          .verifying(
            stopOnFirstFail(
              negativeConstraint("calc.costsAtLegislationStart.errorNegative"),
              decimalPlaceConstraint("calc.costsAtLegislationStart.errorDecimalPlaces"),
              maxMonetaryValueConstraint(errMsgKey = "calc.costsAtLegislationStart.error.tooHigh")
            )
          )
      )
    )(CostsAtLegislationStartModel.apply)(o=>Some(o.hasCosts, o.costs))
  )
}
