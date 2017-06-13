/*
 * Copyright 2017 HM Revenue & Customs
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

import constructors.{PropertyDetailsConstructor, PurchaseDetailsConstructor, SalesDetailsConstructor}

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
                                 costsAtLegislationStart: Option[CostsAtLegislationStartModel] = None) {

  lazy val salesDetailsRows: Seq[QuestionAnswerModel[Any]] = SalesDetailsConstructor.salesDetailsRows(this)
  lazy val purchaseDetailsRows: Seq[QuestionAnswerModel[Any]] = PurchaseDetailsConstructor.getPurchaseDetailsSection(this)
  lazy val propertyDetailsRows: Seq[QuestionAnswerModel[Any]] = PropertyDetailsConstructor.propertyDetailsRows(this)
}
