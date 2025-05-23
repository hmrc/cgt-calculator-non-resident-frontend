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

import play.api.libs.functional.syntax._
import play.api.libs.json._

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
                                 isClaimingImprovementsModel: IsClaimingImprovementsModel,
                                 improvementsModel: Option[ImprovementsModel],
                                 otherReliefsFlat: Option[OtherReliefsModel],
                                 costsAtLegislationStart: Option[CostsAtLegislationStartModel] = None)

object TotalGainAnswersModel {
  private val ignore = OWrites[Any](_ => Json.obj())

  implicit val postWrites: Writes[TotalGainAnswersModel] = (o: TotalGainAnswersModel) => postWrites(o).writes(o)

  private def postWrites(model: TotalGainAnswersModel): Writes[TotalGainAnswersModel] = (
    (__ \ "disposalDate").write[DateModel](using DateModel.postWrites) and
      ignore and
      ignore and
      (__ \ "disposalValue").write[DisposalValueModel](using DisposalValueModel.postWrites) and
      (__ \ "disposalCosts").write[DisposalCostsModel](using DisposalCostsModel.postWrites) and
      ignore and
      ignore and
      (__ \ "acquisitionValue").write[AcquisitionValueModel](using AcquisitionValueModel.postWrites) and
      __.write[Option[AcquisitionCostsModel]](using AcquisitionCostsModel.postWrites(model.costsAtLegislationStart, model.acquisitionDateModel)) and
      (__ \ "acquisitionDate").write[DateModel](using DateModel.postWrites) and
       __.writeNullable[RebasedValueModel](using RebasedValueModel.postWrites(model.acquisitionDateModel)) and
       __.writeNullable[RebasedCostsModel](using RebasedCostsModel.postWrites(model.rebasedValueModel, model.acquisitionDateModel)) and
       __.write[IsClaimingImprovementsModel](using IsClaimingImprovementsModel.postWrites) and
       __.writeNullable[ImprovementsModel](using ImprovementsModel.postWrites(model.rebasedValueModel, model.acquisitionDateModel)) and
      ignore and
      ignore
    ) (o => Tuple.fromProductTyped(o))

}