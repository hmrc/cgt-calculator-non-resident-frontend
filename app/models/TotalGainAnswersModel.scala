/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class TotalGainAnswersModel(disposalDateModel: DateModel,
                                 soldOrGivenAwayModel: SoldOrGivenAwayModel,
                                 soldForLessModel: Option[SoldForLessModel],
                                 disposalValueModel: DisposalValueModel,
                                 disposalCostsModel: DisposalCostsModel,
                                 howBecameOwnerModel: Option[HowBecameOwnerModel],
                                 boughtForLessModel: Option[BoughtForLessModel],
                                 acquisitionValueModel: AcquisitionValueModel,
                                 acquisitionCostsModel: Option[AcquisitionCostsModel],
                                 acquisitionDateModel: DateModel,
                                 rebasedValueModel: Option[RebasedValueModel],
                                 rebasedCostsModel: Option[RebasedCostsModel],
                                 improvementsModel: ImprovementsModel,
                                 otherReliefsFlat: Option[OtherReliefsModel],
                                 costsAtLegislationStart: Option[CostsAtLegislationStartModel] = None)

object TotalGainAnswersModel {
  private val ignore = OWrites[Any](_ => Json.obj())

  implicit val postWrites: Writes[TotalGainAnswersModel] = new Writes[TotalGainAnswersModel] {
    override def writes(o: TotalGainAnswersModel): JsValue =
      postWrites(o).writes(o)
  }

  private def postWrites(model: TotalGainAnswersModel): Writes[TotalGainAnswersModel] = (
    (__ \ "disposalDate").write[DateModel](DateModel.postWrites) and
      ignore and
      ignore and
      (__ \ "disposalValue").write[DisposalValueModel](DisposalValueModel.postWrites) and
      (__ \ "disposalCosts").write[DisposalCostsModel](DisposalCostsModel.postWrites) and
      ignore and
      ignore and
      (__ \ "acquisitionValue").write[AcquisitionValueModel](AcquisitionValueModel.postWrites) and
      __.write[Option[AcquisitionCostsModel]](AcquisitionCostsModel.postWrites(model.costsAtLegislationStart, model.acquisitionDateModel)) and
      (__ \ "acquisitionDate").write[DateModel](DateModel.postWrites) and
       __.writeNullable[RebasedValueModel](RebasedValueModel.postWrites(model.acquisitionDateModel)) and
       __.writeNullable[RebasedCostsModel](RebasedCostsModel.postWrites(model.rebasedValueModel, model.acquisitionDateModel)) and
       __.write[ImprovementsModel](ImprovementsModel.postWrites(model.rebasedValueModel, model.acquisitionDateModel)) and
      ignore and
      ignore
    ) (unlift(TotalGainAnswersModel.unapply))
}