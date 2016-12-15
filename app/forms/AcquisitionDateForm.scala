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

import common.Validation._
import models.AcquisitionDateModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object AcquisitionDateForm {

  val acquisitionDateForm = Form(
    mapping(
      "hasAcquisitionDate" -> text
        .verifying(Messages("calc.common.error.fieldRequired"), mandatoryCheck)
        .verifying(Messages("calc.common.error.fieldRequired"), yesNoCheck),
      "acquisitionDateDay" -> optional(number),
      "acquisitionDateMonth" -> optional(number),
      "acquisitionDateYear" -> optional(number)
    )(AcquisitionDateModel.apply)(AcquisitionDateModel.unapply).verifying(Messages("calc.common.date.error.invalidDate"), fields =>
      if(fields.hasAcquisitionDate == "Yes") {isValidDate(fields.day.getOrElse(0), fields.month.getOrElse(0), fields.year.getOrElse(0))} else true))
}