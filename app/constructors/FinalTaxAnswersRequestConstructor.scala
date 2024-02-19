/*
 * Copyright 2023 HM Revenue & Customs
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

package constructors

import common.YesNoKeys
import common.nonresident._
import models._

import scala.math.BigDecimal

object FinalTaxAnswersRequestConstructor {

  def additionalParametersQuery(model: Option[TotalPersonalDetailsCalculationModel],
                                maxAnnualExemptAmount: BigDecimal): String = {
    currentIncome(model.map {_.currentIncomeModel}) +
      personalAllowanceAmt(model.flatMap {_.personalAllowanceModel}) +
      allowableLoss(model.map {_.otherPropertiesModel}, model.flatMap {_.previousGainOrLoss}, model.flatMap {_.howMuchLossModel}) +
      previousGain(model.map {_.otherPropertiesModel}, model.flatMap {_.previousGainOrLoss}, model.flatMap {_.howMuchGainModel}) +
      broughtForwardLosses(model.map {_.broughtForwardLossesModel}) +
      annualExemptAmount(model.map {_.otherPropertiesModel}, model.flatMap {_.previousGainOrLoss},
        model.flatMap {_.howMuchLossModel}, model.flatMap {_.howMuchGainModel}, model.flatMap {_.annualExemptAmountModel}, maxAnnualExemptAmount)
  }

  def currentIncome(currentIncomeModel: Option[CurrentIncomeModel]): String = {
    currentIncomeModel match {
      case Some(data) => s"&currentIncome=${data.currentIncome.toDouble}"
      case _ => ""
    }
  }

  def personalAllowanceAmt(personalAllowanceModel: Option[PersonalAllowanceModel]): String =
    personalAllowanceModel match {
      case Some(data) => s"&personalAllowanceAmt=${data.personalAllowanceAmt.toDouble}"
      case _ => ""
    }

  def allowableLoss(otherPropertiesModel: Option[OtherPropertiesModel],
                    previousLossOrGainModel: Option[PreviousLossOrGainModel],
                    howMuchLossModel: Option[HowMuchLossModel]): String = {
    (otherPropertiesModel, previousLossOrGainModel) match {
      case (Some(OtherPropertiesModel(YesNoKeys.yes)), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss))) =>
        s"&allowableLoss=${howMuchLossModel.get.loss.toDouble}"
      case _ => ""
    }
  }

  def previousGain(otherPropertiesModel: Option[OtherPropertiesModel],
                   previousLossOrGainModel: Option[PreviousLossOrGainModel],
                   howMuchGainModel: Option[HowMuchGainModel]): String = {
    (otherPropertiesModel, previousLossOrGainModel) match {
      case (Some(OtherPropertiesModel(YesNoKeys.yes)),Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain))) =>
        s"&previousGain=${howMuchGainModel.get.howMuchGain.toDouble}"
      case _ => ""
    }
  }

  def annualExemptAmount(otherPropertiesModel: Option[OtherPropertiesModel],
                         previousLossOrGainModel: Option[PreviousLossOrGainModel],
                         howMuchLossModel: Option[HowMuchLossModel],
                         howMuchGainModel: Option[HowMuchGainModel],
                         annualExemptAmountModel: Option[AnnualExemptAmountModel],
                         maxAnnualExemptAmount: BigDecimal): String = {

    (otherPropertiesModel, previousLossOrGainModel, howMuchLossModel, howMuchGainModel) match {

      case (Some(OtherPropertiesModel(YesNoKeys.yes)), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)),
      Some(HowMuchLossModel(loss)), _) if loss == 0.0 => s"&annualExemptAmount=${annualExemptAmountModel.get.annualExemptAmount.toDouble}"
      case (Some(OtherPropertiesModel(YesNoKeys.yes)), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)), _,
      Some(HowMuchGainModel(gain))) if gain == 0.0 => s"&annualExemptAmount=${annualExemptAmountModel.get.annualExemptAmount.toDouble}"
      case (Some(OtherPropertiesModel(YesNoKeys.yes)), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)), _,
      Some(HowMuchGainModel(_))) => "&annualExemptAmount=0"
      case (Some(OtherPropertiesModel(YesNoKeys.yes)), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.neither)), _, _) =>
        s"&annualExemptAmount=${annualExemptAmountModel.get.annualExemptAmount.toDouble}"
      case (_, _, _, _) =>
        s"&annualExemptAmount=${maxAnnualExemptAmount.toDouble}"
    }
  }

  def broughtForwardLosses(model: Option[BroughtForwardLossesModel]): String = {
    if (model.isDefined && model.get.isClaiming) {
      s"&broughtForwardLoss=${model.get.broughtForwardLoss.get.toDouble}"
    } else {
      ""
    }
  }
}
