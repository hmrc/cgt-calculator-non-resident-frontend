/*
 * Copyright 2020 HM Revenue & Customs
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

import common.TaxDates
import play.api.libs.json._
import constructors.TotalGainRequestConstructor.includeLegislationCosts
import constructors.TotalGainRequestConstructor.afterLegislation

case class AcquisitionCostsModel (acquisitionCostsAmt: BigDecimal)

object AcquisitionCostsModel {
  implicit val format = Json.format[AcquisitionCostsModel]
  implicit val convertToSome: AcquisitionCostsModel => Option[AcquisitionCostsModel] = model => Some(model)


  def postWrites(oCostsAtLegislationStartModel: Option[CostsAtLegislationStartModel], acquisitionDateModel: DateModel):
    Writes[Option[AcquisitionCostsModel]] = new Writes[Option[AcquisitionCostsModel]] {
    override def writes(o: Option[AcquisitionCostsModel])= {
        (o, oCostsAtLegislationStartModel) match {
          case (_, Some(value)) if includeLegislationCosts(value, acquisitionDateModel)=>
            Json.obj(("acquisitionCosts",value.costs.get))
          case (Some(value), _) if afterLegislation(acquisitionDateModel) =>
            Json.obj(("acquisitionCosts", value.acquisitionCostsAmt))
          case _ => Json.obj(("acquisitionCosts", 0))
        }
      }
    }
  }

