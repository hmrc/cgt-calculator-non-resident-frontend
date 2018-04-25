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
import common.Transformers._
import common.Validation._
import models.AcquisitionCostsModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object AcquisitionCostsForm {

  val acquisitionCostsForm = Form(
    mapping(
      "acquisitionCosts" -> text
        .verifying("error.real", mandatoryCheck)
        .verifying("error.real", bigDecimalCheck)
        .transform(stringToBigDecimal, bigDecimalToString)
        .verifying("calc.acquisitionCosts.errorNegative", isPositive)
        .verifying("calc.acquisitionCosts.errorDecimalPlaces", decimalPlacesCheck)
        .verifying("calc.common.error.maxNumericExceeded" + MoneyPounds(Constants.maxNumeric, 0).quantity + " " +
          "calc.common.error.maxNumericExceeded.OrLess", maxCheck)
    )(AcquisitionCostsModel.apply)(AcquisitionCostsModel.unapply)
  )

}
