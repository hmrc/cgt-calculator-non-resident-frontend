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
import models.DisposalValueModel
import play.api.data.Forms._
import play.api.data._

object DisposalValueForm {

  val disposalValueForm = Form(
    mapping(
      "disposalValue" -> text
        .verifying("error.real", mandatoryCheck)
        .verifying("error.real", bigDecimalCheck)
        .transform(stringToBigDecimal, bigDecimalToString)
        .verifying("calc.disposalValue.errorNegative", isPositive)
        .verifying("calc.disposalValue.errorDecimalPlaces", decimalPlacesCheck)
        .verifying(maxMonetaryValueConstraint(Constants.maxNumeric))
    )(DisposalValueModel.apply)(DisposalValueModel.unapply)
  )
}
