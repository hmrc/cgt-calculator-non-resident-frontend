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

package models

import common.Dates
import play.api.libs.json.{Json, JsValue, OFormat, Writes}
import java.time.LocalDate
import scala.util.{Try, Success}

case class DateModel(day: Int, month: Int, year: Int) {

  def isDateAfter(referenceDate: DateModel): Boolean =
    (year > referenceDate.year) ||
      (year == referenceDate.year && month > referenceDate.month) ||
      (year == referenceDate.year && month == referenceDate.month && day > referenceDate.day)
}

object DateModel {
  given OFormat[DateModel] = Json.format[DateModel]

  val postWrites: Writes[DateModel] = (model: DateModel) => Json.toJson(LocalDate.of(model.year, model.month, model.day))

  given Conversion[DateModel, Option[LocalDate]] with
    def apply(model: DateModel): Option[LocalDate] =
      val dateFormatter = Dates.formatter
      Try {
        LocalDate.parse(s"${model.day}/${model.month}/${model.year}", dateFormatter)
      } match {
        case Success(date) => Some(date)
        case _ => None

      }
}