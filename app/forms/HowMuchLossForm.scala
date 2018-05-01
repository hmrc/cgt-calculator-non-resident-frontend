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
import models.HowMuchLossModel
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object HowMuchLossForm {

  val howMuchLossForm: Form[HowMuchLossModel] = Form(
    mapping(
      "loss" -> text
        .verifying("calc.common.error.mandatoryAmount", mandatoryCheck)
        .verifying("calc.common.error.mandatoryAmount", bigDecimalCheck)
        .transform(stringToBigDecimal, bigDecimalToString)
        .verifying("calc.howMuchLoss.errorMinimum", isPositive)
        .verifying("calc.howMuchLoss.errorDecimal", decimalPlacesCheck)
        .verifying("calc.common.error.maxNumericExceeded" + MoneyPounds(Constants.maxNumeric, 0).quantity + " " +
          "calc.common.error.maxNumericExceeded.OrLess", maxCheck)
    )(HowMuchLossModel.apply)(HowMuchLossModel.unapply)
  )
}
