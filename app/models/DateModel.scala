/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.LocalDate

import common.Dates
import play.api.libs.json.{JsValue, Json, Writes}

import scala.util.{Success, Try}

case class DateModel(day: Int, month: Int, year: Int) {

  def isDateAfter(referenceDate: DateModel): Boolean = {
    (year > referenceDate.year) ||
    (year == referenceDate.year && month > referenceDate.month) ||
    (year == referenceDate.year && month == referenceDate.month && day > referenceDate.day)
  }
}

object DateModel {
  implicit val format = Json.format[DateModel]

  implicit val createDate: DateModel => Option[LocalDate] = model => {
    val dateFormatter = Dates.formatter
    Try {
      LocalDate.parse(s"${model.day}/${model.month}/${model.year}", dateFormatter)
    } match {
      case Success(date) => Some(date)
      case _ => None
    }
  }

  val postWrites = new Writes[DateModel] {
    override def writes(model: DateModel): JsValue = {
      Json.toJson(LocalDate.of(model.year, model.month, model.day))
    }
  }
}
