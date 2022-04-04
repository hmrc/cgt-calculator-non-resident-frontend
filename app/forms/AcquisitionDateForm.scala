/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.{ZoneId, ZonedDateTime}

import common.Validation._
import models.DateModel
import play.api.data.Forms._
import play.api.data._
import common.Transformers._

object AcquisitionDateForm {

  val acquisitionDateForm = Form(
    mapping(
      "acquisitionDateDay" -> text
        .verifying("calc.common.date.invalidDayError", mandatoryCheck)
        .verifying("calc.common.date.error.invalidDate", integerCheck)
        .transform[Int](stringToInteger, _.toString),
      "acquisitionDateMonth" -> text
        .verifying("calc.common.date.invalidMonthError", mandatoryCheck)
        .verifying("calc.common.date.error.invalidDate", integerCheck)
        .transform[Int](stringToInteger, _.toString),
      "acquisitionDateYear" -> text
        .verifying("calc.common.date.invalidYearError", mandatoryCheck)
        .verifying("calc.common.date.error.invalidDate", integerCheck)
        .transform[Int](stringToInteger, _.toString)
    )(DateModel.apply)(DateModel.unapply)
      .verifying("calc.common.date.error.invalidDate", fields =>
        isValidDate(fields.day, fields.month, fields.year))
      .verifying("calc.acquisitionDate.errorFutureDateGuidance", fields =>
        verifyDateInPast(fields))
  )

  def verifyDateInPast(data: DateModel): Boolean = {
    if(isValidDate(data.day, data.month, data.year)) data.get.isBefore(ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDate)
    else true
  }
}