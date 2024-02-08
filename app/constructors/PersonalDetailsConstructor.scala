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

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.nonresident.PreviousGainOrLossKeys
import javax.inject.Inject
import models._
import play.api.i18n.{Messages, MessagesProvider}

import scala.math.BigDecimal

class PersonalDetailsConstructor @Inject()(implicit messagesProvider: MessagesProvider) {

  def getPersonalDetailsSection(summaryModel: Option[TotalPersonalDetailsCalculationModel]): Seq[QuestionAnswerModel[Any]] = {

    summaryModel match {
      case Some(data) =>
        val currentIncomeData = getCurrentIncomeAnswer(data.currentIncomeModel)
        val personalAllowanceData = getPersonalAllowanceAnswer(data.personalAllowanceModel, data.currentIncomeModel)
        val otherPropertiesData = getOtherPropertiesAnswer(data.otherPropertiesModel)
        val previousGainOrLossData = previousGainOrLossAnswer(data.otherPropertiesModel, data.previousGainOrLoss)
        val howMuchGainData = howMuchGainAnswer(data.otherPropertiesModel, data.previousGainOrLoss, data.howMuchGainModel)
        val howMuchLossData = howMuchLossAnswer(data.otherPropertiesModel, data.previousGainOrLoss, data.howMuchLossModel)
        val annualExemptAmountData = getAnnualExemptAmountAnswer(data.otherPropertiesModel,
          data.previousGainOrLoss, data.annualExemptAmountModel, data.howMuchGainModel, data.howMuchLossModel)
        val broughtForwardLossesQuestionData = getBroughtForwardLossesQuestion(data.broughtForwardLossesModel)
        val broughtForwardLossesAnswerData = getBroughtForwardLossesAnswer(data.broughtForwardLossesModel)

        val items = Seq(
          currentIncomeData,
          personalAllowanceData,
          otherPropertiesData,
          previousGainOrLossData,
          howMuchGainData,
          howMuchLossData,
          annualExemptAmountData,
          broughtForwardLossesQuestionData,
          broughtForwardLossesAnswerData
        )

        items.flatten
      case _ => Seq()
    }


  }

  //Customer type needs to be individual
  def getCurrentIncomeAnswer(currentIncomeModel: CurrentIncomeModel): Option[QuestionAnswerModel[BigDecimal]] =
    Some(QuestionAnswerModel(
      KeystoreKeys.currentIncome,
      currentIncomeModel.currentIncome,
      Messages("calc.currentIncome.question"),
      Some(controllers.routes.CurrentIncomeController.currentIncome.url))
    )

  //Customer type needs to be individual
  def getPersonalAllowanceAnswer(personalAllowanceModel: Option[PersonalAllowanceModel],
                                 currentIncomeModel: CurrentIncomeModel): Option[QuestionAnswerModel[BigDecimal]] = {

    personalAllowanceModel match {
      case Some(PersonalAllowanceModel(value)) =>
        if (currentIncomeModel.currentIncome > 0) {
          Some(QuestionAnswerModel(
            KeystoreKeys.personalAllowance,
            value,
            Messages("calc.personalAllowance.question"),
            Some(controllers.routes.PersonalAllowanceController.personalAllowance.url))
          )
        }
        else None
      case _ => None
    }
  }

  def getOtherPropertiesAnswer(otherPropertiesModel: OtherPropertiesModel): Option[QuestionAnswerModel[String]] = {
    Some(QuestionAnswerModel(
      KeystoreKeys.otherProperties,
      otherPropertiesModel.otherProperties,
      Messages("calc.otherProperties.question"),
      Some(controllers.routes.OtherPropertiesController.otherProperties.url)
    ))
  }

  def previousGainOrLossAnswer(otherPropertiesModel: OtherPropertiesModel,
                               previousLossOrGainModel: Option[PreviousLossOrGainModel]): Option[QuestionAnswerModel[String]] = {
    otherPropertiesModel match {
      case OtherPropertiesModel("Yes") =>
        Some(QuestionAnswerModel(
          KeystoreKeys.previousLossOrGain,
          previousLossOrGainModel.get.previousLossOrGain,
          Messages("calc.previousLossOrGain.question"),
          Some(controllers.routes.PreviousGainOrLossController.previousGainOrLoss.url)
        ))
      case _ => None
    }
  }

  def howMuchGainAnswer(otherPropertiesModel: OtherPropertiesModel,
                        previousLossOrGainModel: Option[PreviousLossOrGainModel],
                        howMuchGainModel: Option[HowMuchGainModel]): Option[QuestionAnswerModel[BigDecimal]] = {
    (otherPropertiesModel, previousLossOrGainModel) match {
      case (OtherPropertiesModel("Yes"), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain))) =>
        Some(QuestionAnswerModel(
          KeystoreKeys.howMuchGain,
          howMuchGainModel.get.howMuchGain,
          Messages("calc.howMuchGain.question"),
          Some(controllers.routes.HowMuchGainController.howMuchGain.url)
        ))
      case _ => None
    }
  }

  def howMuchLossAnswer(otherPropertiesModel: OtherPropertiesModel,
                        previousLossOrGainModel: Option[PreviousLossOrGainModel],
                        howMuchLossModel: Option[HowMuchLossModel]): Option[QuestionAnswerModel[BigDecimal]] = {
    (otherPropertiesModel, previousLossOrGainModel) match {
      case (OtherPropertiesModel("Yes"), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss))) =>
        Some(QuestionAnswerModel(
          KeystoreKeys.howMuchLoss,
          howMuchLossModel.get.loss,
          Messages("calc.howMuchLoss.question"),
          Some(controllers.routes.HowMuchLossController.howMuchLoss.url)
        ))
      case _ => None
    }
  }

  def getAnnualExemptAmountAnswer(otherPropertiesModel: OtherPropertiesModel,
                                  previousLossOrGainModel: Option[PreviousLossOrGainModel],
                                  annualExemptAmountModel: Option[AnnualExemptAmountModel],
                                  howMuchGainModel: Option[HowMuchGainModel],
                                  howMuchLossModel: Option[HowMuchLossModel]): Option[QuestionAnswerModel[BigDecimal]] = {
    val id = KeystoreKeys.annualExemptAmount
    val question = Messages("calc.annualExemptAmount.question")
    val route = Some(controllers.routes.AnnualExemptAmountController.annualExemptAmount.url)

    (otherPropertiesModel, previousLossOrGainModel) match {
      case (OtherPropertiesModel("Yes"), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.neither))) =>
        Some(QuestionAnswerModel(
          id,
          annualExemptAmountModel.get.annualExemptAmount,
          question,
          route)
        )
      case (OtherPropertiesModel("Yes"), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)))
        if howMuchGainModel.get.howMuchGain == 0 =>
        Some(QuestionAnswerModel(
          id,
          annualExemptAmountModel.get.annualExemptAmount,
          question,
          route)
        )
      case (OtherPropertiesModel("Yes"), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)))
        if howMuchLossModel.get.loss == 0 =>
        Some(QuestionAnswerModel(
          id,
          annualExemptAmountModel.get.annualExemptAmount,
          question,
          route)
        )
      case _ => None
    }
  }

  def getBroughtForwardLossesQuestion(broughtForwardLossesModel: BroughtForwardLossesModel): Option[QuestionAnswerModel[Boolean]] = {
    Some(QuestionAnswerModel(
      s"${KeystoreKeys.broughtForwardLosses}-question",
      broughtForwardLossesModel.isClaiming,
      Messages("calc.broughtForwardLosses.question"),
      Some(controllers.routes.BroughtForwardLossesController.broughtForwardLosses.url)
    ))
  }

  def getBroughtForwardLossesAnswer(broughtForwardLossesModel: BroughtForwardLossesModel): Option[QuestionAnswerModel[BigDecimal]] = {
    broughtForwardLossesModel match {
      case BroughtForwardLossesModel(true, Some(data)) =>
        Some(QuestionAnswerModel(
          KeystoreKeys.broughtForwardLosses,
          data,
          Messages("calc.broughtForwardLosses.inputQuestion"),
          Some(controllers.routes.BroughtForwardLossesController.broughtForwardLosses.url)
        ))
      case _ => None
    }
  }

}
