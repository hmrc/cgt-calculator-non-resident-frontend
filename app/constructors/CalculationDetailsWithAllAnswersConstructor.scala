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

import common.nonresident._
import models.{CalculationResultsWithTaxOwedModel, QuestionAnswerModel}
import play.api.i18n.{Messages, MessagesProvider}

import javax.inject.Inject


class CalculationDetailsWithAllAnswersConstructor @Inject()(val calculationDetailsConstructor: CalculationDetailsConstructor,
                                                            val calculationDetailsWithPRRConstructor: CalculationDetailsWithPRRConstructor)
                                                           (implicit messagesProvider: MessagesProvider) {

  def buildSection(calculation: CalculationResultsWithTaxOwedModel, calculationType: CalculationType, taxYear: String): Seq[QuestionAnswerModel[Any]] = {
    val electionDetails = calculationDetailsConstructor.calculationElection(calculationType)
    val correctModel = calculationType match {
      case Flat => calculation.flatResult
      case Rebased => calculation.rebasedResult.get
      case TimeApportioned => calculation.timeApportionedResult.get
    }
    val taxableGainDetails = calculationDetailsWithPRRConstructor.taxableGain(correctModel.taxableGain)
    val totalGainDetails = calculationDetailsConstructor.totalGain(correctModel.totalGain)
    val totalLossDetails = calculationDetailsConstructor.totalLoss(correctModel.totalGain)
    val prrDetails = correctModel.prrUsed match {
      case Some(value) => calculationDetailsWithPRRConstructor.prrUsedDetails(value)
      case _ => None
    }
    val otherReliefsUsed = otherReliefsUsedRow(correctModel.otherReliefsUsed)
    val allowableLossesUsed = allowableLossesUsedRow(correctModel.allowableLossesUsed, taxYear)
    val broughtForwardLossesUsed = broughtForwardLossesUsedRow(correctModel.broughtForwardLossesUsed, taxYear)
    val annualExemptAmountUsed = aeaUsedRow(correctModel.aeaUsed)
    val annualExemptAmountRemaining = aeaRemainingRow(correctModel.aeaRemaining)
    val lossesRemaining = lossesRemainingRow(correctModel.taxableGain)
    val taxRates = taxRatesRow(correctModel.taxGain, correctModel.taxRate, correctModel.upperTaxGain, correctModel.upperTaxRate)

    Seq(
      electionDetails,
      totalGainDetails,
      totalLossDetails,
      prrDetails,
      otherReliefsUsed,
      allowableLossesUsed,
      annualExemptAmountUsed,
      annualExemptAmountRemaining,
      broughtForwardLossesUsed,
      taxableGainDetails,
      lossesRemaining,
      taxRates
    ).flatten
  }

  def allowableLossesUsedRow(allowableLossUsed: Option[BigDecimal], taxYear: String): Option[QuestionAnswerModel[BigDecimal]] = {
    allowableLossUsed match {
      case Some(data) if data > 0 =>
        val id = "calcDetails:allowableLossesUsed"
        val question = Messages("calc.summary.calculation.details.allowableLossesUsed", taxYear)
        Some(QuestionAnswerModel(id, data, question, None))
      case _ => None
    }
  }

  def aeaUsedRow(annualExemptAmountUsed: Option[BigDecimal]): Option[QuestionAnswerModel[BigDecimal]] = {
    annualExemptAmountUsed match {
      case Some(data) if data > 0 =>
        val id = "calcDetails:annualExemptAmountUsed"
        val question = Messages("calc.summary.calculation.details.usedAEA")
        Some(QuestionAnswerModel(id, data, question, None))
      case _ => None
    }
  }

  def aeaRemainingRow(annualExemptAmountRemaining: BigDecimal): Option[QuestionAnswerModel[BigDecimal]] = {
    val id = "calcDetails:annualExemptAmountRemaining"
    val question = Messages("calc.summary.calculation.details.remainingAEA")
    Some(QuestionAnswerModel(id, annualExemptAmountRemaining, question, None))
  }

  def broughtForwardLossesUsedRow(broughtForwardLossesUsed: Option[BigDecimal], taxYear: String): Option[QuestionAnswerModel[BigDecimal]] = {
    broughtForwardLossesUsed match {
      case Some(data) if data > 0 =>
        val id = "calcDetails:broughtForwardLossesUsed"
        val question = Messages("calc.summary.calculation.details.broughtForwardLossesUsed", taxYear)
        Some(QuestionAnswerModel(id, data, question, None))
      case _ => None
    }
  }

  def lossesRemainingRow(taxableGain: BigDecimal): Option[QuestionAnswerModel[BigDecimal]] = {
    //TODO add correct wording from designers to messages.en
    if (taxableGain < 0) {
      val id = "calcDetails:lossesRemaining"
      val question = Messages("calc.summary.calculation.details.lossesRemaining")
      Some(QuestionAnswerModel(id, taxableGain.abs, question, None))
    } else None
  }

  def taxRatesRow(taxableGain: BigDecimal,
                  taxRate: Int,
                  additionalTaxableGain: Option[BigDecimal],
                  additionalTaxRate: Option[Int]): Option[QuestionAnswerModel[Any]] = {
    val id = "calcDetails:taxRate"
    val question = Messages("calc.summary.calculation.details.taxRate")

    (taxableGain, additionalTaxableGain) match {
      case (_, Some(additionalGain)) if taxableGain > 0 =>
        val value = (taxableGain, taxRate, additionalGain, additionalTaxRate.get)
        Some(QuestionAnswerModel(id, value, question, None))
      case (_, Some(additionalGain)) if additionalGain > 0 =>
        val value = Messages("calc.summary.calculation.details.taxRateValue", s"£${MoneyPounds(additionalGain, 2).quantity}", additionalTaxRate.get)
        Some(QuestionAnswerModel(id, value, question, None))
      case _ if taxableGain > 0 =>
        val value = Messages("calc.summary.calculation.details.taxRateValue", s"£${MoneyPounds(taxableGain, 2).quantity}", taxRate)
        Some(QuestionAnswerModel(id, value, question, None))
      case _ => None
    }
  }

  def otherReliefsUsedRow(otherReliefsUsed: Option[BigDecimal]): Option[QuestionAnswerModel[BigDecimal]] = {
    otherReliefsUsed match {
      case Some(value) if value > 0 =>
        val id = "calcDetails:otherReliefsUsed"
        val question = Messages("calc.summary.calculation.details.otherReliefsUsed")
        Some(QuestionAnswerModel(id, value, question, None))
      case _ => None
    }
  }
}
