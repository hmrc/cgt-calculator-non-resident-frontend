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

import common.Constants
import common.Transformers._
import common.Validation._
import models.RebasedValueModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object RebasedValueForm {

  def mandatoryField(data: Option[String], required: Boolean): Boolean = !(required && data.isEmpty)


  def rebasedValueForm(required: Boolean): Form[RebasedValueModel] = Form(
    mapping(
      "rebasedValueAmt" -> optional(text)
        .verifying(Messages("calc.nonResident.rebasedValue.error.no.value.supplied"), data => mandatoryField(data, required))
        .verifying(Messages("error.number"), data => bigDecimalCheck(data.getOrElse("")))
        .transform[Option[BigDecimal]](optionalStringToOptionalBigDecimal, optionalBigDecimalToOptionalString)
        .verifying(Messages("calc.nonResident.rebasedValue.errorNegative"), data => isPositive(data.getOrElse(0)))
        .verifying(Messages("calc.nonResident.rebasedValue.errorDecimalPlaces"), data => decimalPlacesCheck(data.getOrElse(0)))
        .verifying(Messages("calc.common.error.maxAmountExceeded", MoneyPounds(Constants.maxNumeric, 0).quantity), data => maxCheck(data.getOrElse(0)))
    )(RebasedValueModel.apply)(RebasedValueModel.unapply)
  )
}