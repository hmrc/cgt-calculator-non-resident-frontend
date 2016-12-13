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

package models.nonresident

import java.time.LocalDate

import common.Dates
import play.api.libs.json.Json

import scala.util.{Success, Try}

case class DisposalDateModel (day: Int, month: Int, year: Int)

object DisposalDateModel {
  implicit val format = Json.format[DisposalDateModel]

  implicit val createDate: DisposalDateModel => Option[LocalDate] = model => {
    val dateFormatter = Dates.formatter
    Try {
      LocalDate.parse(s"${model.day}/${model.month}/${model.year}", dateFormatter)
    } match {
      case Success(date) => Some(date)
      case _ => None
    }
  }

}
