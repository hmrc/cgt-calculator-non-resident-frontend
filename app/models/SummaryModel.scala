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

import common.nonresident.{CalculationType, Flat, Rebased, TimeApportioned}

case class SummaryModel(
                         currentIncomeModel: CurrentIncomeModel,
                         personalAllowanceModel: Option[PersonalAllowanceModel],
                         otherPropertiesModel: OtherPropertiesModel,
                         annualExemptAmountModel: Option[AnnualExemptAmountModel],
                         acquisitionDateModel: DateModel,
                         acquisitionValueModel: AcquisitionValueModel,
                         rebasedValueModel: Option[RebasedValueModel],
                         rebasedCostsModel: Option[RebasedCostsModel],
                         improvementsModel: ImprovementsModel,
                         disposalDateModel: DateModel,
                         disposalValueModel: DisposalValueModel,
                         acquisitionCostsModel: AcquisitionCostsModel,
                         disposalCostsModel: DisposalCostsModel,
                         calculationElectionModel: CalculationElectionModel,
                         otherReliefsModelFlat: OtherReliefsModel,
                         otherReliefsModelTA: OtherReliefsModel,
                         otherReliefsModelRebased: OtherReliefsModel,
                         privateResidenceReliefModel: Option[PrivateResidenceReliefModel]
                       ) {

  def reliefApplied(): CalculationType = calculationElectionModel.calculationType match {
    case Flat if otherReliefsModelFlat.otherReliefs > 0 => Flat
    case Rebased if otherReliefsModelRebased.otherReliefs > 0 => Rebased
    case TimeApportioned if otherReliefsModelTA.otherReliefs > 0 => TimeApportioned
  }

  val personalDetailsRows: Seq[QuestionAnswerModel[Any]] = Seq(QuestionAnswerModel[String]("", "", "", None))
  val saleDetailsRows: Seq[QuestionAnswerModel[Any]] = Seq(QuestionAnswerModel[String]("", "", "", None))
  val purchaseDetailsRows: Seq[QuestionAnswerModel[Any]] = Seq(QuestionAnswerModel[String]("", "", "", None))
  val propertyDetailsRows: Seq[QuestionAnswerModel[Any]] = Seq(QuestionAnswerModel[String]("", "", "", None))

  def deductionsDetailsRows(result: CalculationResultModel): Seq[QuestionAnswerModel[Any]] =
    Seq(QuestionAnswerModel[String]("", "", "", None))
}
