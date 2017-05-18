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

package constructors

import common.YesNoKeys
import common.nonresident._
import models._

import scala.math.BigDecimal

object FinalTaxAnswersRequestConstructor {

  def additionalParametersQuery(model: TotalPersonalDetailsCalculationModel,
                                maxAnnualExemptAmount: BigDecimal): String = {
    customerType(model.customerTypeModel) +
    isVulnerable(model.customerTypeModel, model.trusteeModel) +
    currentIncome(model.customerTypeModel, model.currentIncomeModel) +
    personalAllowanceAmt(model.customerTypeModel, model.personalAllowanceModel) +
    allowableLoss(model.otherPropertiesModel, model.previousGainOrLoss, model.howMuchLossModel) +
    previousGain(model.otherPropertiesModel, model.previousGainOrLoss, model.howMuchGainModel) +
    broughtForwardLosses(model.broughtForwardLossesModel) +
    annualExemptAmount(model.otherPropertiesModel, model.previousGainOrLoss, model.howMuchLossModel, model.howMuchGainModel,
      model.annualExemptAmountModel, maxAnnualExemptAmount)
  }

  def customerType(model: CustomerTypeModel): String = {
    s"&customerType=${model.customerType}"
  }

  def isVulnerable(customerTypeModel: CustomerTypeModel, disabledTrusteeModel: Option[DisabledTrusteeModel]): String = {

    if(customerTypeModel.customerType == CustomerTypeKeys.trustee) {
      s"&isVulnerable=${disabledTrusteeModel.get.isVulnerable}"
    } else { "" }
  }

  def currentIncome(customerTypeModel: CustomerTypeModel, currentIncomeModel: Option[CurrentIncomeModel]): String = {
    if(customerTypeModel.customerType == CustomerTypeKeys.individual) {
      s"&currentIncome=${currentIncomeModel.getOrElse(CurrentIncomeModel(0)).currentIncome}"
    } else { "" }
  }

  def personalAllowanceAmt(customerTypeModel: CustomerTypeModel, personalAllowanceModel: Option[PersonalAllowanceModel]): String =
    (customerTypeModel.customerType, personalAllowanceModel) match {
        case (CustomerTypeKeys.individual, Some(model)) => s"&personalAllowanceAmt=${model.personalAllowanceAmt}"
        case _ => ""
      }


  def allowableLoss(otherPropertiesModel: OtherPropertiesModel,
                    previousLossOrGainModel: Option[PreviousLossOrGainModel],
                    howMuchLossModel: Option[HowMuchLossModel]): String = {
    (otherPropertiesModel.otherProperties, previousLossOrGainModel) match {
      case (YesNoKeys.yes, Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss))) => s"&allowableLoss=${howMuchLossModel.get.loss}"
      case _ => ""
    }
  }

  def previousGain(otherPropertiesModel: OtherPropertiesModel,
                   previousLossOrGainModel: Option[PreviousLossOrGainModel],
                   howMuchGainModel: Option[HowMuchGainModel]): String = {
    (otherPropertiesModel.otherProperties, previousLossOrGainModel) match {
      case (YesNoKeys.yes, Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain))) => s"&previousGain=${howMuchGainModel.get.howMuchGain}"
      case _ => ""
    }
  }

  def annualExemptAmount(otherPropertiesModel: OtherPropertiesModel,
                         previousLossOrGainModel: Option[PreviousLossOrGainModel],
                         howMuchLossModel: Option[HowMuchLossModel],
                         howMuchGainModel: Option[HowMuchGainModel],
                         annualExemptAmountModel: Option[AnnualExemptAmountModel],
                         maxAnnualExemptAmount: BigDecimal): String = {

    (otherPropertiesModel.otherProperties, previousLossOrGainModel, howMuchLossModel, howMuchGainModel) match {

      case (YesNoKeys.yes, Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)), Some(HowMuchLossModel(loss)), _) if loss == 0.0 =>
        s"&annualExemptAmount=${annualExemptAmountModel.get.annualExemptAmount}"
      case (YesNoKeys.yes, Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)), _, Some(HowMuchGainModel(gain))) if gain == 0.0 =>
        s"&annualExemptAmount=${annualExemptAmountModel.get.annualExemptAmount}"
      case (YesNoKeys.yes, Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)), _, Some(HowMuchGainModel(_))) => "&annualExemptAmount=0"
      case (YesNoKeys.yes, Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.neither)), _, _) =>
        s"&annualExemptAmount=${annualExemptAmountModel.get.annualExemptAmount}"
      case (_, _, _, _) =>
        s"&annualExemptAmount=$maxAnnualExemptAmount"
    }
  }

  def broughtForwardLosses(model: BroughtForwardLossesModel): String = {
    if (model.isClaiming) {
      s"&broughtForwardLoss=${model.broughtForwardLoss.get}"
    } else { "" }
  }
}
