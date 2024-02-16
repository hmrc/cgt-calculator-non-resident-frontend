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

import play.api.libs.json.{JsValue, Json, Writes}

case class IsClaimingImprovementsModel (isClaimingImprovements: Boolean)

object IsClaimingImprovementsModel {
  implicit val format = Json.format[IsClaimingImprovementsModel]

  val postWrites = new Writes[IsClaimingImprovementsModel] {
    override def writes(model: IsClaimingImprovementsModel): JsValue = {
      Json.toJson(model.isClaimingImprovements)
    }
  }

}