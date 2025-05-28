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

import forms.formatters.DateFormatter
import models.DateModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages

import java.time.LocalDate

object AcquisitionDateForm {

  val key = "acquisitionDate"

  def acquisitionDateForm(implicit messages: Messages): Form[DateModel] = Form(
    mapping(
      key -> of(using DateFormatter(
        key,
        optMaxDate = Some(LocalDate.now)
      ))
    )(date => DateModel(date.getDayOfMonth, date.getMonthValue, date.getYear))(model => Some(LocalDate.of(model.year, model.month, model.day)))
  )
}