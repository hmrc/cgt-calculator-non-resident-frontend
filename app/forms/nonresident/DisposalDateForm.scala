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

package forms.nonresident

import java.time.LocalDate

import common.Dates._
import common.Transformers._
import common.Validation._
import models.nonresident.DisposalDateModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages

object DisposalDateForm {

  val disposalDateForm = Form(
    mapping(
      "disposalDateDay" -> text
        .verifying(Messages("calc.common.date.error.invalidDate"), mandatoryCheck)
        .verifying(Messages("calc.common.date.error.invalidDate"), integerCheck)
        .transform[Int](stringToInteger, _.toString),
      "disposalDateMonth" -> text
        .verifying(Messages("calc.common.date.error.invalidDate"), mandatoryCheck)
        .verifying(Messages("calc.common.date.error.invalidDate"), integerCheck)
        .transform[Int](stringToInteger, _.toString),
      "disposalDateYear" -> text
        .verifying(Messages("calc.common.date.error.invalidDate"), mandatoryCheck)
        .verifying(Messages("calc.common.date.error.invalidDate"), integerCheck)
        .transform[Int](stringToInteger, _.toString)
    )(DisposalDateModel.apply)(DisposalDateModel.unapply)
      .verifying(Messages("calc.common.date.error.invalidDate"), fields =>
        isValidDate(fields.day, fields.month, fields.year))
  )
}
