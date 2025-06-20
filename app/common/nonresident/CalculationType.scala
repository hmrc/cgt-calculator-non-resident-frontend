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

package common.nonresident

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json._


object CalculationType {

  implicit val jsonFormat: Format[CalculationType] = new Format[CalculationType] {
    override def reads(json: JsValue): JsResult[CalculationType] = json match {
      case JsString("flat")               => JsSuccess(Flat)
      case JsString("rebased")             => JsSuccess(Rebased)
      case JsString("timeApportioned") => JsSuccess(TimeApportioned)
      case _                                    => JsError("Invalid Calculation type")
    }

    override def writes(o: CalculationType): JsValue = JsString(o.toString)
  }


  implicit val formatter: Formatter[CalculationType] = new Formatter[CalculationType] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], CalculationType] = {
      val bindCalculationTypeFn: PartialFunction[String,CalculationType] = {
        case "flat" => Flat
        case "rebased" => Rebased
        case "timeApportioned" => TimeApportioned
      }

      data.get(key)
        .flatMap(bindCalculationTypeFn.lift).map(Right(_))
        .getOrElse(Left(Seq(FormError(key, s"calc.$key.errors.required"))))
    }

    override def unbind(key: String, value: CalculationType): Map[String, String] =
      value match {
        case Flat => Map(key -> "flat")
        case Rebased => Map(key -> "rebased")
        case TimeApportioned => Map(key -> "timeApportioned")
      }
  }
}

sealed trait CalculationType

case object Flat extends CalculationType {
  override def toString: String = "flat"
}
case object Rebased extends CalculationType {
  override def toString: String = "rebased"
}
case object TimeApportioned extends CalculationType {
  override def toString: String = "timeApportioned"
}
