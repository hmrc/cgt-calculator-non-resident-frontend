/*
 * Copyright 2017 HM Revenue & Customs
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

import java.time.LocalDate

import common.Validation._
import models.AcquisitionDateModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import common.Transformers._

object AcquisitionDateForm {

  val acquisitionDateForm = Form(
    mapping(
      "acquisitionDateDay" -> text
        .verifying(Messages("calc.common.date.error.invalidDate"), mandatoryCheck)
        .verifying(Messages("calc.common.date.error.invalidDate"), integerCheck)
        .transform[Int](stringToInteger, _.toString),
      "acquisitionDateMonth" -> text
        .verifying(Messages("calc.common.date.error.invalidDate"), mandatoryCheck)
        .verifying(Messages("calc.common.date.error.invalidDate"), integerCheck)
        .transform[Int](stringToInteger, _.toString),
      "acquisitionDateYear" -> text
        .verifying(Messages("calc.common.date.error.invalidDate"), mandatoryCheck)
        .verifying(Messages("calc.common.date.error.invalidDate"), integerCheck)
        .transform[Int](stringToInteger, _.toString)
    )(AcquisitionDateModel.apply)(AcquisitionDateModel.unapply)
      .verifying(Messages("calc.common.date.error.invalidDate"), fields =>
        isValidDate(fields.day, fields.month, fields.year))
      .verifying(Messages("calc.nonResident.rebasedValue.errorFutureDate"), fields =>
        verifyDateInPast(fields))
  )

  def verifyDateInPast(data: AcquisitionDateModel): Boolean = {
    if(isValidDate(data.day, data.month, data.year)) data.get.isBefore(LocalDate.now())
    else true
  }
}
