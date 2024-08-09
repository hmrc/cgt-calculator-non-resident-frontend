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

package constructors

import common.YesNoKeys
import common.nonresident._
import models._

object FinalTaxAnswersRequestConstructor {

  def additionalParametersQuery(model: Option[TotalPersonalDetailsCalculationModel],
                                maxAnnualExemptAmount: BigDecimal): Map[String, Option[String]] = {
    model.map { data =>
      val allowableLoss = {
        (data.otherPropertiesModel, data.previousGainOrLoss) match {
          case (OtherPropertiesModel(YesNoKeys.yes), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss))) =>
            Some(data.howMuchLossModel.get.loss)
          case _ => None
        }
      }
      val previousGain = {
        (data.otherPropertiesModel, data.previousGainOrLoss) match {
          case (OtherPropertiesModel(YesNoKeys.yes), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain))) =>
            Some(data.howMuchGainModel.get.howMuchGain)
          case _ => None
        }
      }
      val broughtForwardLosses =
        data.broughtForwardLossesModel match {
          case BroughtForwardLossesModel(true, broughtForwardLoss) => Some(broughtForwardLoss)
          case _ => None
        }
      val annualExemptAmountValue = annualExemptAmount(
        data.otherPropertiesModel,
        data.previousGainOrLoss,
        data.howMuchLossModel,
        data.howMuchGainModel,
        data.annualExemptAmountModel,
        maxAnnualExemptAmount)
      Map(
        "currentIncome" -> Some(data.currentIncomeModel.currentIncome.toString()),
        "personalAllowanceAmt" -> data.personalAllowanceModel.map(_.personalAllowanceAmt.toString()),
        "allowableLoss" -> allowableLoss.map(_.toString()),
        "previousGain" -> previousGain.map(_.toString()),
        "broughtForwardLoss" -> broughtForwardLosses.map(_.toString),
        "annualExemptAmount" -> Some(annualExemptAmountValue.toString)
      )
    }.getOrElse(Map("annualExemptAmount" -> Some(maxAnnualExemptAmount.toString())))
  }

  def annualExemptAmount(otherPropertiesModel: OtherPropertiesModel,
                         previousLossOrGainModel: Option[PreviousLossOrGainModel],
                         howMuchLossModel: Option[HowMuchLossModel],
                         howMuchGainModel: Option[HowMuchGainModel],
                         annualExemptAmountModel: Option[AnnualExemptAmountModel],
                         maxAnnualExemptAmount: BigDecimal): BigDecimal = {

    if (otherPropertiesModel.otherProperties == YesNoKeys.yes) {
      (previousLossOrGainModel, howMuchLossModel, howMuchGainModel) match {
        case (Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)), Some(HowMuchLossModel(loss)), _) if loss == 0 =>
          annualExemptAmountModel.get.annualExemptAmount
        case (Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)), _, Some(HowMuchGainModel(gain))) if gain == 0 =>
          annualExemptAmountModel.get.annualExemptAmount
        case (Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.neither)), _, _) =>
          annualExemptAmountModel.get.annualExemptAmount
        case (Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)), _, Some(HowMuchGainModel(_))) => BigDecimal(0)
        case (_, _, _) => maxAnnualExemptAmount
      }
    } else maxAnnualExemptAmount
  }
}
