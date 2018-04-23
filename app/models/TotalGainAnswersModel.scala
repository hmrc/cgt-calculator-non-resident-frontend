/*
 * Copyright 2018 HM Revenue & Customs
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

case class TotalGainAnswersModel(disposalDateModel: DisposalDateModel,
                                 soldOrGivenAwayModel: SoldOrGivenAwayModel,
                                 soldForLessModel: Option[SoldForLessModel],
                                 disposalValueModel: DisposalValueModel,
                                 disposalCostsModel: DisposalCostsModel,
                                 howBecameOwnerModel: Option[HowBecameOwnerModel],
                                 boughtForLessModel: Option[BoughtForLessModel],
                                 acquisitionValueModel: AcquisitionValueModel,
                                 acquisitionCostsModel: Option[AcquisitionCostsModel],
                                 acquisitionDateModel: AcquisitionDateModel,
                                 rebasedValueModel: Option[RebasedValueModel],
                                 rebasedCostsModel: Option[RebasedCostsModel],
                                 improvementsModel: ImprovementsModel,
                                 otherReliefsFlat: Option[OtherReliefsModel],
                                 costsAtLegislationStart: Option[CostsAtLegislationStartModel] = None)

object TotalGainAnswersModel {
  private val ignore = OWrites[Any](_ => Json.obj())

  implicit val totalGainWrites: Writes[TotalGainAnswersModel] = (
    (__ \ "disposalDate").write[DisposalDateModel](DisposalDateModel.postWrites) and
      ignore and
      ignore and
      (__ \ "disposalValue").write[DisposalValueModel](DisposalValueModel.postWrites) and
      (__ \ "disposalCosts").write[DisposalCostsModel](DisposalCostsModel.postWrites) and
      ignore and
      ignore and
      (__ \ "acquisitionValue").write[AcquisitionValueModel](AcquisitionValueModel.postWrites) and
      (__ \ "acquisitionCosts").writeNullable[AcquisitionCostsModel](AcquisitionCostsModel.postWrites) and
      (__ \ "acquisitionDate").write[AcquisitionDateModel](AcquisitionDateModel.postWrites) and
      (__ \ "rebasedValue").writeNullable[RebasedValueModel](RebasedValueModel.postWrites) and
      (__ \ "rebasedCosts").writeNullable[RebasedCostsModel](RebasedCostsModel.postWrites) and
      __.write[ImprovementsModel](ImprovementsModel.postWrites) and
      ignore and
      ignore
    ) (unlift(TotalGainAnswersModel.unapply))
}