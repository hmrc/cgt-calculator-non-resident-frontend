/*
 * Copyright 2021 HM Revenue & Customs
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
import models.AcquisitionValueModel
import play.api.data.Forms._
import play.api.data._

object AcquisitionValueForm {

  val acquisitionValueForm = Form(
    mapping(
      "acquisitionValue" -> text
        .verifying("error.real", mandatoryCheck)
        .verifying("error.real", bigDecimalCheck)
        .transform(stringToBigDecimal, bigDecimalToString)
        .verifying("calc.acquisitionValue.errorNegative", isPositive)
        .verifying("calc.acquisitionValue.errorDecimalPlaces", decimalPlacesCheck)
        .verifying(maxMonetaryValueConstraint(Constants.maxNumeric))
    )(AcquisitionValueModel.apply)(AcquisitionValueModel.unapply)
  )
}
