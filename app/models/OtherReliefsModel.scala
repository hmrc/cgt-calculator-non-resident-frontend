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

import play.api.libs.json.{JsValue, Json, OFormat, Writes}

case class OtherReliefsModel (otherReliefs: BigDecimal)

object OtherReliefsModel {
  implicit val format: OFormat[OtherReliefsModel] = Json.format[OtherReliefsModel]

  val postWrites: Writes[OtherReliefsModel] = new Writes[OtherReliefsModel] {
    override def writes(model: OtherReliefsModel): JsValue = {
      Json.toJson(model.otherReliefs)
    }
  }
}
