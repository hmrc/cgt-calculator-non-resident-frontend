/*
 * Copyright 2020 HM Revenue & Customs
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

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.nonresident.PreviousGainOrLossKeys
import common.{Transformers, YesNoKeys}
import models._

object PersonalAndPreviousDetailsConstructor {

  def personalAndPreviousDetailsRows(personalAndPreviousDetailsModel: Option[TotalPersonalDetailsCalculationModel]): Seq[QuestionAnswerModel[Any]] = {

    personalAndPreviousDetailsModel match {
      case None => Seq()
      case Some(data) => constructRows(data)
    }
  }

  def constructRows(personalAndPreviousDetailsModel: TotalPersonalDetailsCalculationModel): Seq[QuestionAnswerModel[Any]] = {

    val currentIncomeAnswerData = currentIncomeAnswerRow(personalAndPreviousDetailsModel.currentIncomeModel)
    val personalAllowanceAnswerData = personalAllowanceAnswerRow(
      personalAndPreviousDetailsModel.currentIncomeModel,
      personalAndPreviousDetailsModel.personalAllowanceModel)
    val otherPropertiesAnswerData = otherPropertiesAnswerRow(personalAndPreviousDetailsModel.otherPropertiesModel)
    val previousGainsOrLossAnswerData = previousGainsOrLossAnswerRow(
      personalAndPreviousDetailsModel.otherPropertiesModel,
      personalAndPreviousDetailsModel.previousGainOrLoss)
    val howMuchLossAnswerData = howMuchLossAnswerRow(
      personalAndPreviousDetailsModel.otherPropertiesModel,
      personalAndPreviousDetailsModel.previousGainOrLoss,
      personalAndPreviousDetailsModel.howMuchLossModel)
    val howMuchGainAnswerData = howMuchGainAnswerRow(
      personalAndPreviousDetailsModel.otherPropertiesModel,
      personalAndPreviousDetailsModel.previousGainOrLoss,
      personalAndPreviousDetailsModel.howMuchGainModel)
    val annualExemptAmountAnswerData = annualExemptAmountAnswerRow(
      personalAndPreviousDetailsModel.otherPropertiesModel,
      personalAndPreviousDetailsModel.previousGainOrLoss,
      personalAndPreviousDetailsModel.howMuchLossModel,
      personalAndPreviousDetailsModel.howMuchGainModel,
      personalAndPreviousDetailsModel.annualExemptAmountModel)
    val broughtForwardLossesAnswerData = broughtForwardLossesAnswerRow(personalAndPreviousDetailsModel.broughtForwardLossesModel)
    val broughtForwardLossesValueAnswerData = broughtForwardLossesValueAnswerRow(personalAndPreviousDetailsModel.broughtForwardLossesModel)

    Seq(
      currentIncomeAnswerData,
      personalAllowanceAnswerData,
      otherPropertiesAnswerData,
      previousGainsOrLossAnswerData,
      howMuchLossAnswerData,
      howMuchGainAnswerData,
      annualExemptAmountAnswerData,
      broughtForwardLossesAnswerData,
      broughtForwardLossesValueAnswerData
    ).flatten
  }

  def currentIncomeAnswerRow(currentIncomeModel: CurrentIncomeModel): Option[QuestionAnswerModel[BigDecimal]] = {
    Some(QuestionAnswerModel(
      s"${KeystoreKeys.currentIncome}-question",
      currentIncomeModel.currentIncome,
      "calc.currentIncome.question",
      Some(controllers.routes.CurrentIncomeController.currentIncome().url)
    ))
  }

  def personalAllowanceAnswerRow(currentIncomeModel: CurrentIncomeModel,
                                 personalAllowanceModel: Option[PersonalAllowanceModel]): Option[QuestionAnswerModel[BigDecimal]] = {

    personalAllowanceModel match {
      case (Some(data)) if currentIncomeModel.currentIncome > 0 =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.personalAllowance}-question",
          data.personalAllowanceAmt,
          "calc.personalAllowance.question",
          Some(controllers.routes.PersonalAllowanceController.personalAllowance().url)
        ))
      case _ => None
    }
  }

  def otherPropertiesAnswerRow(otherPropertiesModel: OtherPropertiesModel): Option[QuestionAnswerModel[String]] = {

    Some(QuestionAnswerModel(
      s"${KeystoreKeys.otherProperties}-question",
      otherPropertiesModel.otherProperties,
      "calc.otherProperties.question",
      Some(controllers.routes.OtherPropertiesController.otherProperties().url)
    ))
  }

  def previousGainsOrLossAnswerRow(otherPropertiesModel: OtherPropertiesModel,
                                   previousLossOrGainModel: Option[PreviousLossOrGainModel]): Option[QuestionAnswerModel[String]] = {

    val message = previousLossOrGainModel match {
      case Some(model) if model.previousLossOrGain == PreviousGainOrLossKeys.gain => "calc.previousLossOrGain.gain"
      case Some(model) if model.previousLossOrGain == PreviousGainOrLossKeys.loss => "calc.previousLossOrGain.loss"
      case Some(model) if model.previousLossOrGain == PreviousGainOrLossKeys.neither => "calc.previousLossOrGain.neither"
      case _ => ""
    }

    otherPropertiesModel.otherProperties match {
      case YesNoKeys.yes =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.previousLossOrGain}-question",
          message,
          "calc.previousLossOrGain.question",
          Some(controllers.routes.PreviousGainOrLossController.previousGainOrLoss().url)
        ))
      case _ => None
    }
  }

  def howMuchLossAnswerRow(otherPropertiesModel: OtherPropertiesModel,
                           previousLossOrGainModel: Option[PreviousLossOrGainModel],
                           howMuchLossModel: Option[HowMuchLossModel]): Option[QuestionAnswerModel[BigDecimal]] = {

    (otherPropertiesModel.otherProperties, previousLossOrGainModel) match {
      case (YesNoKeys.yes, Some(data)) if data.previousLossOrGain == PreviousGainOrLossKeys.loss =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.howMuchLoss}-question",
          howMuchLossModel.get.loss,
          "calc.howMuchLoss.question",
          Some(controllers.routes.HowMuchLossController.howMuchLoss().url)
        ))
      case (_, _) => None
    }
  }

  def howMuchGainAnswerRow(otherPropertiesModel: OtherPropertiesModel,
                           previousLossOrGainModel: Option[PreviousLossOrGainModel],
                           howMuchGainModel: Option[HowMuchGainModel]): Option[QuestionAnswerModel[BigDecimal]] = {

    (otherPropertiesModel.otherProperties, previousLossOrGainModel) match {
      case (YesNoKeys.yes, Some(data)) if data.previousLossOrGain == PreviousGainOrLossKeys.gain =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.howMuchGain}-question",
          howMuchGainModel.get.howMuchGain,
          "calc.howMuchGain.question",
          Some(controllers.routes.HowMuchGainController.howMuchGain().url)
        ))
      case (_, _) => None
    }
  }

  def annualExemptAmountAnswerRow(otherPropertiesModel: OtherPropertiesModel,
                                  previousLossOrGainModel: Option[PreviousLossOrGainModel],
                                  howMuchLossModel: Option[HowMuchLossModel],
                                  howMuchGainModel: Option[HowMuchGainModel],
                                  annualExemptAmountModel: Option[AnnualExemptAmountModel]): Option[QuestionAnswerModel[BigDecimal]] = {

    def annualExemptAmountElement: Option[QuestionAnswerModel[BigDecimal]] = {
      Some(QuestionAnswerModel(
        s"${KeystoreKeys.annualExemptAmount}-question",
        annualExemptAmountModel.get.annualExemptAmount,
        "calc.annualExemptAmount.question",
        Some(controllers.routes.AnnualExemptAmountController.annualExemptAmount().url)
      ))
    }

    (otherPropertiesModel.otherProperties, previousLossOrGainModel, howMuchLossModel, howMuchGainModel) match {

      case (YesNoKeys.yes, Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)), Some(HowMuchLossModel(loss)), _) if loss == 0.0 =>
        annualExemptAmountElement
      case (YesNoKeys.yes, Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)), _, Some(HowMuchGainModel(gain))) if gain == 0.0 =>
        annualExemptAmountElement
      case (YesNoKeys.yes, Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.neither)), _, _) =>
        annualExemptAmountElement
      case (_, _, _, _) =>
        None
    }
  }

  def broughtForwardLossesAnswerRow(broughtForwardLossesModel: BroughtForwardLossesModel): Option[QuestionAnswerModel[String]] = {
    Some(QuestionAnswerModel(
      s"${KeystoreKeys.broughtForwardLosses}-question",
      Transformers.booleanToString(broughtForwardLossesModel.isClaiming),
      "calc.broughtForwardLosses.question",
      Some(controllers.routes.BroughtForwardLossesController.broughtForwardLosses().url)
    ))
  }

  def broughtForwardLossesValueAnswerRow(broughtForwardLossesModel: BroughtForwardLossesModel): Option[QuestionAnswerModel[BigDecimal]] = {

    if (broughtForwardLossesModel.isClaiming) {
      Some(QuestionAnswerModel(
        s"${KeystoreKeys.broughtForwardLosses}-value-question",
        broughtForwardLossesModel.broughtForwardLoss.get,
        "calc.broughtForwardLosses.inputQuestion",
        Some(controllers.routes.BroughtForwardLossesController.broughtForwardLosses().url)
      ))
    }
    else {
      None
    }
  }
}
