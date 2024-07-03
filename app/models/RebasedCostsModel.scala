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
import play.api.libs.json.{JsNumber, Json, OFormat, Writes}

case class RebasedCostsModel(hasRebasedCosts: String, rebasedCosts: Option[BigDecimal])

object RebasedCostsModel {
  implicit val format: OFormat[RebasedCostsModel] = Json.format[RebasedCostsModel]


  def postWrites(oRebasedValueModel: Option[RebasedValueModel], acquisitionDateModel: DateModel): Writes[RebasedCostsModel] = (model: RebasedCostsModel) => {
    if (includeRebasedValuesInCalculation(oRebasedValueModel, acquisitionDateModel)) {
      model match {
        case RebasedCostsModel("Yes", Some(value)) => Json.obj(("rebasedCosts", JsNumber(value)))
        case _ => Json.obj()
      }
    } else {
      Json.obj()
    }
  }
}
