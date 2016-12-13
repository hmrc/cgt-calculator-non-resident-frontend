/*
 * Copyright 2016 HM Revenue & Customs
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

package constructors.nonresident

import common.TaxDates.dateAfter18Months
import models.nonresident._

import scala.math.BigDecimal

object CalculateRequestConstructor {

  def baseCalcUrl(input: SummaryModel): String = {
    customerType(input.customerTypeModel.customerType) +
    priorDisposal(input.otherPropertiesModel.otherProperties) +
    annualExemptAmount(input.otherPropertiesModel, input.annualExemptAmountModel) +
    otherPropertiesAmount(input.otherPropertiesModel) +
    isVulnerableTrustee(input.customerTypeModel.customerType, input.disabledTrusteeModel) +
    currentIncome(input.customerTypeModel.customerType, input.currentIncomeModel) +
    personalAllowanceAmount(input.customerTypeModel.customerType, input.personalAllowanceModel) +
    disposalValue(input.disposalValueModel.disposalValue) +
    disposalCosts(input.disposalCostsModel.disposalCosts) +
    allowableLossesAmount(input.allowableLossesModel) +
    disposalDate(input.disposalDateModel)
  }

  def customerType(customerType: String): String = s"customerType=$customerType"

  def priorDisposal(otherProperties: String): String = s"&priorDisposal=$otherProperties"

  def annualExemptAmount(otherPropertiesModel: OtherPropertiesModel, annualExemptAmountModel: Option[AnnualExemptAmountModel]): String = {
    otherPropertiesModel match {
      //case OtherPropertiesModel("Yes", Some(data)) if data == 0 => s"&annualExemptAmount=${annualExemptAmountModel.get.annualExemptAmount}"
      case _ => ""
    }
  }

  def otherPropertiesAmount(otherPropertiesModel: OtherPropertiesModel): String = {
    otherPropertiesModel match {
      //case OtherPropertiesModel("Yes", Some(data)) => s"&otherPropertiesAmt=$data"
      case _ => ""
    }
  }

  def isVulnerableTrustee(customerType: String, disabledTrusteeModel: Option[DisabledTrusteeModel]): String = {
    if (customerType.equals("trustee")) s"&isVulnerable=${disabledTrusteeModel.get.isVulnerable}"
    else ""
  }

  def currentIncome(customerType: String, currentIncomeModel: Option[CurrentIncomeModel]): String = {
    if (customerType.equals("individual")) s"&currentIncome=${currentIncomeModel.get.currentIncome}"
    else ""
  }

  def personalAllowanceAmount(customerType: String, personalAllowanceModel: Option[PersonalAllowanceModel]): String = {
    if (customerType.equals("individual") && personalAllowanceModel.isDefined) s"&personalAllowanceAmt=${personalAllowanceModel.get.personalAllowanceAmt}"
    else ""
  }

  def disposalValue(disposalValue: BigDecimal): String = s"&disposalValue=$disposalValue"

  def disposalCosts(disposalCosts: BigDecimal): String = s"&disposalCosts=$disposalCosts"

  def allowableLossesAmount(allowableLossesModel: AllowableLossesModel): String = {
    s"&allowableLossesAmt=${
      if (allowableLossesModel.isClaimingAllowableLosses.equals("Yes")) allowableLossesModel.allowableLossesAmt.get
      else "0"
    }"
  }

  def disposalDate(disposalDateModel: DisposalDateModel): String = {
    s"&disposalDate=${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}"
  }

  def flatCalcUrlExtra(input: SummaryModel): String = {
    val isClaimingPrr = isClaimingPRR(input)
    improvements(input) +
      acquisition(input) +
      flatReliefs(Some(input.otherReliefsModelFlat.otherReliefs)) +
      privateResidenceReliefFlat(input) +
      isClaimingPrr +
      flatAcquisitionDate(isClaimingPrr, input.acquisitionDateModel)
  }

  def flatReliefs(reliefsValue: Option[BigDecimal]): String = {
    s"&reliefs=${reliefsValue.getOrElse(0)}"
  }

  def flatAcquisitionDate(isClaimingPrr: String, acquisitionDateModel: AcquisitionDateModel): String = {
    if (isClaimingPrr.contains("Yes"))
      s"&acquisitionDate=${acquisitionDateModel.year.get}-${acquisitionDateModel.month.get}-${acquisitionDateModel.day.get}"
    else ""
  }

  def taCalcUrlExtra(input: SummaryModel): String = {
    improvements(input) +
      taAcquisitionDate(input.acquisitionDateModel) +
      acquisition(input) +
      taReliefs(Some(input.otherReliefsModelTA.otherReliefs)) +
      privateResidenceReliefTA(input) +
      isClaimingPRR(input)
  }

  def taAcquisitionDate(acquisitionDateModel: AcquisitionDateModel): String = {
    s"&acquisitionDate=${acquisitionDateModel.year.get}-${acquisitionDateModel.month.get}-${acquisitionDateModel.day.get}"
  }

  def taReliefs(otherReliefs: Option[BigDecimal]): String = {
    s"&reliefs=${otherReliefs.getOrElse(0)}"
  }

  def rebasedCalcUrlExtra(input: SummaryModel): String = {
    rebasedImprovements(input.improvementsModel) +
      rebasedValue(input.rebasedValueModel.get.rebasedValueAmt.get) +
      revaluationCost(input.rebasedCostsModel.get) +
      rebasedReliefs(Some(input.otherReliefsModelRebased.otherReliefs)) +
      privateResidenceReliefRebased(input) +
      isClaimingPrrRebased(input.privateResidenceReliefModel)
  }

  def rebasedImprovements(improvementsModel: ImprovementsModel): String = {
    s"&improvementsAmt=${
      if (improvementsModel.isClaimingImprovements.equals("Yes")) improvementsModel.improvementsAmtAfter.getOrElse(0)
      else 0
    }"
  }

  def rebasedValue(value: BigDecimal): String = {
    s"&initialValueAmt=$value"
  }

  def revaluationCost(rebasedCostsModel: RebasedCostsModel): String = {
    s"&initialCostsAmt=${
      if (rebasedCostsModel.hasRebasedCosts.equals("Yes")) rebasedCostsModel.rebasedCosts.get
      else 0
    }"
  }

  def rebasedReliefs(reliefsValue: Option[BigDecimal]): String = {
    s"&reliefs=${reliefsValue.getOrElse(0)}"
  }

  def isClaimingPrrRebased(privateResidenceReliefModel: Option[PrivateResidenceReliefModel]): String = {
    s"&isClaimingPRR=${
      if (privateResidenceReliefModel.isDefined) privateResidenceReliefModel.get.isClaimingPRR
      else "No"
    }"
  }

  def improvements(input: SummaryModel): String = s"&improvementsAmt=${
    (input.improvementsModel.isClaimingImprovements, input.rebasedValueModel) match {
      case ("Yes", Some(RebasedValueModel(data))) if data.isDefined => input.improvementsModel.improvementsAmtAfter.getOrElse(BigDecimal(0)) +
        input.improvementsModel.improvementsAmt.getOrElse(BigDecimal(0))
      case ("No", _) => 0
      case _ => input.improvementsModel.improvementsAmt.getOrElse(0)
    }
  }"

  def privateResidenceReliefFlat(input: SummaryModel): String = s"${
    (input.acquisitionDateModel, input.privateResidenceReliefModel) match {
      case (AcquisitionDateModel("Yes", day, month, year), Some(PrivateResidenceReliefModel("Yes", claimed, after))) if claimed.isDefined =>
        s"&daysClaimed=${claimed.get}"
      case _ => ""
    }
  }"

  def privateResidenceReliefTA(input: SummaryModel): String = s"${
    (input.acquisitionDateModel, input.privateResidenceReliefModel) match {
      case (AcquisitionDateModel("Yes", day, month, year), Some(PrivateResidenceReliefModel("Yes", claimed, after)))
        if dateAfter18Months(input.disposalDateModel.day, input.disposalDateModel.month, input.disposalDateModel.year) && after.isDefined =>
        s"&daysClaimed=${after.get}"

      case _ => ""
    }
  }"

  def privateResidenceReliefRebased(input: SummaryModel): String = s"${
    (input.rebasedValueModel, input.privateResidenceReliefModel) match {
      case (Some(RebasedValueModel(rebasedValue)), Some(PrivateResidenceReliefModel("Yes", claimed, after)))
        if dateAfter18Months(input.disposalDateModel.day, input.disposalDateModel.month, input.disposalDateModel.year) &&
          after.isDefined && rebasedValue.isDefined =>
        s"&daysClaimed=${after.get}"
      case _ => ""
    }
  }"

  def isClaimingPRR(input: SummaryModel): String = s"&isClaimingPRR=${
    (input.acquisitionDateModel, input.privateResidenceReliefModel) match {
      case (AcquisitionDateModel("Yes", day, month, year), Some(PrivateResidenceReliefModel("Yes", claimed, after))) => "Yes"
      case _ => "No"
    }
  }"

  def acquisition(input: SummaryModel): String = s"&initialValueAmt=${
    input.acquisitionValueModel.acquisitionValueAmt
  }&initialCostsAmt=${
    input.acquisitionCostsModel.acquisitionCostsAmt
  }"
}
