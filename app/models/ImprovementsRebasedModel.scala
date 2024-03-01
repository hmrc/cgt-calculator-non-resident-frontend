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

import constructors.TotalGainRequestConstructor.includeRebasedValuesInCalculation
import play.api.libs.json.{JsObject, JsValue, Json, Writes}

case class ImprovementsRebasedModel(improvementsAmt: BigDecimal, improvementsAmtAfter: BigDecimal)

object ImprovementsRebasedModel {
  implicit val format = Json.format[ImprovementsRebasedModel]
  implicit val convertToSome: ImprovementsRebasedModel => Option[ImprovementsRebasedModel] = model => Some(model)


  def postWrites(oRebasedValueModel: Option[RebasedValueModel], acquisitionDateModel: DateModel): Writes[ImprovementsRebasedModel] = new Writes[ImprovementsRebasedModel] {
    override def writes(o: ImprovementsRebasedModel): JsValue = {
      improvementsRebasedWrites.writes(o).as[JsObject] ++
      improvementsAftWrites(oRebasedValueModel, acquisitionDateModel).writes(o).as[JsObject]
    }
  }

  private def improvementsAftWrites(oRebasedValueModel: Option[RebasedValueModel], acquisitionDateModel: DateModel):
    Writes[ImprovementsRebasedModel] = new Writes[ImprovementsRebasedModel] {
      override def writes(o: ImprovementsRebasedModel): JsValue = {
        o match {
          case ImprovementsRebasedModel(_, value1) if includeRebasedValuesInCalculation(oRebasedValueModel, acquisitionDateModel) =>
            Json.obj(("improvementsAfterTaxStarted", value1))
          case _ => Json.obj()
        }
      }
    }

  private val improvementsRebasedWrites = new Writes[ImprovementsRebasedModel] {
    override def writes(o: ImprovementsRebasedModel): JsValue = {
      o match {
        case ImprovementsRebasedModel(value, _) => Json.obj(("improvements", value))
        case _ => Json.obj()
      }
    }
  }
}
