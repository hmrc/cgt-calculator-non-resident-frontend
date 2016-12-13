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

import common.{KeystoreKeys, Transformers, YesNoKeys}
import common.nonresident.{CustomerTypeKeys, PreviousGainOrLossKeys}
import models.nonresident._
import play.api.i18n.Messages

object PersonalAndPreviousDetailsConstructor {

  def personalAndPreviousDetailsRows(personalAndPreviousDetailsModel: Option[TotalPersonalDetailsCalculationModel]): Seq[QuestionAnswerModel[Any]] = {

    personalAndPreviousDetailsModel match {
      case None => Seq()
      case Some(data) => constructRows(data)
    }
  }

  def constructRows(personalAndPreviousDetailsModel: TotalPersonalDetailsCalculationModel): Seq[QuestionAnswerModel[Any]] = {

    val customerTypeAnswerData = customerTypeAnswerRow(personalAndPreviousDetailsModel.customerTypeModel)
    val currentIncomeAnswerData = currentIncomeAnswerRow(
      personalAndPreviousDetailsModel.customerTypeModel,
      personalAndPreviousDetailsModel.currentIncomeModel)
    val personalAllowanceAnswerData = personalAllowanceAnswerRow(
      personalAndPreviousDetailsModel.customerTypeModel,
      personalAndPreviousDetailsModel.personalAllowanceModel)
    val disabledTrusteeAnswerData = disabledTrusteeAnswerRow(
      personalAndPreviousDetailsModel.customerTypeModel,
      personalAndPreviousDetailsModel.trusteeModel)
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
      customerTypeAnswerData,
      currentIncomeAnswerData,
      personalAllowanceAnswerData,
      disabledTrusteeAnswerData,
      otherPropertiesAnswerData,
      previousGainsOrLossAnswerData,
      howMuchLossAnswerData,
      howMuchGainAnswerData,
      annualExemptAmountAnswerData,
      broughtForwardLossesAnswerData,
      broughtForwardLossesValueAnswerData
    ).flatten
  }

  def customerTypeAnswerRow(customerTypeModel: CustomerTypeModel): Option[QuestionAnswerModel[String]] = {

    val message = customerTypeModel.customerType match {
      case CustomerTypeKeys.individual => Messages("calc.customerType.individual")
      case CustomerTypeKeys.trustee => Messages("calc.customerType.trustee")
      case CustomerTypeKeys.personalRep => Messages("calc.customerType.personalRep")
    }

    Some(QuestionAnswerModel(
      s"${KeystoreKeys.customerType}-question",
      message,
      Messages("calc.customerType.question"),
      Some(controllers.nonresident.routes.CustomerTypeController.customerType().url)
    ))
  }

  def currentIncomeAnswerRow(customerTypeModel: CustomerTypeModel, currentIncomeModel: Option[CurrentIncomeModel]): Option[QuestionAnswerModel[BigDecimal]] = {

    (customerTypeModel.customerType, currentIncomeModel) match {
      case (CustomerTypeKeys.individual, Some(data)) =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.currentIncome}-question",
          data.currentIncome,
          Messages("calc.currentIncome.question"),
          Some(controllers.nonresident.routes.CurrentIncomeController.currentIncome().url)
        ))
      case (_, _) => None
    }
  }

  def personalAllowanceAnswerRow(customerTypeModel: CustomerTypeModel,
                                 personalAllowanceModel: Option[PersonalAllowanceModel]): Option[QuestionAnswerModel[BigDecimal]] = {

    (customerTypeModel.customerType, personalAllowanceModel) match {
      case (CustomerTypeKeys.individual, Some(data)) =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.personalAllowance}-question",
          data.personalAllowanceAmt,
          Messages("calc.personalAllowance.question"),
          Some(controllers.nonresident.routes.PersonalAllowanceController.personalAllowance().url)
        ))
      case (_, _) => None
    }
  }

  def disabledTrusteeAnswerRow(customerTypeModel: CustomerTypeModel,
                               disabledTrusteeModel: Option[DisabledTrusteeModel]): Option[QuestionAnswerModel[String]] = {

    (customerTypeModel.customerType, disabledTrusteeModel) match {
      case (CustomerTypeKeys.trustee, Some(data)) =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.disabledTrustee}-question",
          data.isVulnerable,
          Messages("calc.disabledTrustee.question"),
          Some(controllers.nonresident.routes.DisabledTrusteeController.disabledTrustee().url)
        ))
      case (_, _) => None
    }
  }

  def otherPropertiesAnswerRow(otherPropertiesModel: OtherPropertiesModel): Option[QuestionAnswerModel[String]] = {

    Some(QuestionAnswerModel(
      s"${KeystoreKeys.otherProperties}-question",
      otherPropertiesModel.otherProperties,
      Messages("calc.otherProperties.question"),
      Some(controllers.nonresident.routes.OtherPropertiesController.otherProperties().url)
    ))
  }

  def previousGainsOrLossAnswerRow(otherPropertiesModel: OtherPropertiesModel,
                                   previousLossOrGainModel: Option[PreviousLossOrGainModel]): Option[QuestionAnswerModel[String]] = {

    val message = previousLossOrGainModel match {
      case Some(model) if model.previousLossOrGain == PreviousGainOrLossKeys.gain => Messages("calc.previousLossOrGain.gain")
      case Some(model) if model.previousLossOrGain == PreviousGainOrLossKeys.loss => Messages("calc.previousLossOrGain.loss")
      case Some(model) if model.previousLossOrGain == PreviousGainOrLossKeys.neither => Messages("calc.previousLossOrGain.neither")
      case _ => ""
    }

    otherPropertiesModel.otherProperties match {
      case YesNoKeys.yes =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.NonResidentKeys.previousLossOrGain}-question",
          message,
          Messages("calc.previousLossOrGain.question"),
          Some(controllers.nonresident.routes.PreviousGainOrLossController.previousGainOrLoss().url)
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
          Messages("calc.howMuchLoss.question"),
          Some(controllers.nonresident.routes.HowMuchLossController.howMuchLoss().url)
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
          Messages("calc.howMuchGain.question"),
          Some(controllers.nonresident.routes.HowMuchGainController.howMuchGain().url)
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
        Messages("calc.annualExemptAmount.question"),
        Some(controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url)
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
      Messages("calc.broughtForwardLosses.question"),
      Some(controllers.nonresident.routes.BroughtForwardLossesController.broughtForwardLosses().url)
    ))
  }

  def broughtForwardLossesValueAnswerRow(broughtForwardLossesModel: BroughtForwardLossesModel): Option[QuestionAnswerModel[BigDecimal]] = {

    if (broughtForwardLossesModel.isClaiming) {
      Some(QuestionAnswerModel(
        s"${KeystoreKeys.broughtForwardLosses}-value-question",
        broughtForwardLossesModel.broughtForwardLoss.get,
        Messages("calc.broughtForwardLosses.inputQuestion"),
        Some(controllers.nonresident.routes.BroughtForwardLossesController.broughtForwardLosses().url)
      ))
    }
    else {
      None
    }
  }
}
