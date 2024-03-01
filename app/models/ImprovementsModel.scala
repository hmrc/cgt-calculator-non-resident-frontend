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

import play.api.libs.json.{JsObject, JsValue, Json, Writes}

case class ImprovementsModel(improvementsAmt: BigDecimal)

object ImprovementsModel {
  implicit val format = Json.format[ImprovementsModel]
  //implicit val convertToSome: ImprovementsModel => Option[ImprovementsModel] = model => Some(model)

  val postWrites = new Writes[ImprovementsModel] {
    override def writes(model: ImprovementsModel): JsValue = {
      improvementsWrites.writes(model).as[JsObject]
      Json.toJson(model.improvementsAmt)
    }
  }


  private val improvementsWrites = new Writes[ImprovementsModel] {
    override def writes(o: ImprovementsModel): JsValue = {
      o match {
        case ImprovementsModel(value) => Json.obj(("improvements", value))
        case _ => Json.obj()
      }
    }
  }

}
