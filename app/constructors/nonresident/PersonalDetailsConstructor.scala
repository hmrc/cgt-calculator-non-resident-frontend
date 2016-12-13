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

import common.KeystoreKeys
import common.nonresident.{CustomerTypeKeys, PreviousGainOrLossKeys}
import models.nonresident._
import play.api.i18n.Messages

import scala.math.BigDecimal

object PersonalDetailsConstructor {

  def getPersonalDetailsSection(summaryModel: Option[TotalPersonalDetailsCalculationModel]): Seq[QuestionAnswerModel[Any]] = {

    summaryModel match {
      case Some(data) =>
        val customerTypeData = getCustomerTypeAnswer(data.customerTypeModel)
        val currentIncomeData = getCurrentIncomeAnswer(data.customerTypeModel, data.currentIncomeModel)
        val personalAllowanceData = getPersonalAllowanceAnswer(data.customerTypeModel, data.personalAllowanceModel, data.currentIncomeModel)
        val disabledTrusteeData = getDisabledTrusteeAnswer(data.customerTypeModel, data.trusteeModel)
        val otherPropertiesData = getOtherPropertiesAnswer(data.otherPropertiesModel)
        val previousGainOrLossData = previousGainOrLossAnswer(data.otherPropertiesModel, data.previousGainOrLoss)
        val howMuchGainData = howMuchGainAnswer(data.otherPropertiesModel, data.previousGainOrLoss, data.howMuchGainModel)
        val howMuchLossData = howMuchLossAnswer(data.otherPropertiesModel, data.previousGainOrLoss, data.howMuchLossModel)
        val annualExemptAmountData = getAnnualExemptAmountAnswer(data.otherPropertiesModel,
          data.previousGainOrLoss, data.annualExemptAmountModel, data.howMuchGainModel, data.howMuchLossModel)
        val broughtForwardLossesQuestionData = getBroughtForwardLossesQuestion(data.broughtForwardLossesModel)
        val broughtForwardLossesAnswerData = getBroughtForwardLossesAnswer(data.broughtForwardLossesModel)

        val items = Seq(
          customerTypeData,
          currentIncomeData,
          personalAllowanceData,
          disabledTrusteeData,
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

  def getCustomerTypeAnswer(customerTypeModel: CustomerTypeModel): Option[QuestionAnswerModel[String]] = {
    val answer = customerTypeModel.customerType match {
      case CustomerTypeKeys.individual => Messages("calc.customerType.individual")
      case CustomerTypeKeys.trustee => Messages("calc.customerType.trustee")
      case CustomerTypeKeys.personalRep => Messages("calc.customerType.personalRep")
    }

    Some(QuestionAnswerModel(
      KeystoreKeys.customerType,
      answer,
      Messages("calc.customerType.question"),
      Some(controllers.nonresident.routes.CustomerTypeController.customerType().url)
    ))
  }

  //Customer type needs to be individual
  def getCurrentIncomeAnswer(customerTypeModel: CustomerTypeModel, currentIncomeModel: Option[CurrentIncomeModel]): Option[QuestionAnswerModel[BigDecimal]] =
    (customerTypeModel.customerType, currentIncomeModel) match {
      case (CustomerTypeKeys.individual, Some(CurrentIncomeModel(value))) =>
        Some(QuestionAnswerModel(
          KeystoreKeys.currentIncome,
          value,
          Messages("calc.currentIncome.question"),
          Some(controllers.nonresident.routes.CurrentIncomeController.currentIncome().url))
        )
      case _ => None
    }

  //Customer type needs to be individual
  def getPersonalAllowanceAnswer(customerTypeModel: CustomerTypeModel, personalAllowanceModel: Option[PersonalAllowanceModel],
                                 currentIncomeModel: Option[CurrentIncomeModel]): Option[QuestionAnswerModel[BigDecimal]] = {

    val checkCurrentIncome: BigDecimal =
      currentIncomeModel match {
        case Some(model) => model.currentIncome
        case None => 0
      }

    (customerTypeModel.customerType, personalAllowanceModel) match {
      case (CustomerTypeKeys.individual, Some(PersonalAllowanceModel(value))) =>
        if (checkCurrentIncome > 0) {
          Some(QuestionAnswerModel(
            KeystoreKeys.personalAllowance,
            value,
            Messages("calc.personalAllowance.question"),
            Some(controllers.nonresident.routes.PersonalAllowanceController.personalAllowance().url))
          )
        }
        else None
      case _ => None
    }
  }

  //Customer type needs to be trustee
  def getDisabledTrusteeAnswer(customerTypeModel: CustomerTypeModel, trusteeModel: Option[DisabledTrusteeModel]): Option[QuestionAnswerModel[String]] =
    (customerTypeModel.customerType, trusteeModel) match {
      case (CustomerTypeKeys.trustee, Some(disabledTrusteeModel)) => Some(QuestionAnswerModel(
        KeystoreKeys.disabledTrustee,
        disabledTrusteeModel.isVulnerable,
        Messages("calc.disabledTrustee.question"),
        Some(controllers.nonresident.routes.DisabledTrusteeController.disabledTrustee().url)))
      case _ => None
    }

  def getOtherPropertiesAnswer(otherPropertiesModel: OtherPropertiesModel): Option[QuestionAnswerModel[String]] = {
    Some(QuestionAnswerModel(
      KeystoreKeys.otherProperties,
      otherPropertiesModel.otherProperties,
      Messages("calc.otherProperties.question"),
      Some(controllers.nonresident.routes.OtherPropertiesController.otherProperties().url)
    ))
  }

  def previousGainOrLossAnswer(otherPropertiesModel: OtherPropertiesModel,
                               previousLossOrGainModel: Option[PreviousLossOrGainModel]): Option[QuestionAnswerModel[String]] = {
    otherPropertiesModel match {
      case OtherPropertiesModel("Yes") =>
        Some(QuestionAnswerModel(
          KeystoreKeys.NonResidentKeys.previousLossOrGain,
          previousLossOrGainModel.get.previousLossOrGain,
          Messages("calc.previousLossOrGain.question"),
          Some(controllers.nonresident.routes.PreviousGainOrLossController.previousGainOrLoss().url)
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
          Some(controllers.nonresident.routes.HowMuchGainController.howMuchGain().url)
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
          Some(controllers.nonresident.routes.HowMuchLossController.howMuchLoss().url)
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
    val route = Some(controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url)

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
      Some(controllers.nonresident.routes.BroughtForwardLossesController.broughtForwardLosses().url)
    ))
  }

  def getBroughtForwardLossesAnswer(broughtForwardLossesModel: BroughtForwardLossesModel): Option[QuestionAnswerModel[BigDecimal]] = {
    broughtForwardLossesModel match {
      case BroughtForwardLossesModel(true, Some(data)) =>
        Some(QuestionAnswerModel(
          KeystoreKeys.broughtForwardLosses,
          data,
          Messages("calc.broughtForwardLosses.inputQuestion"),
          Some(controllers.nonresident.routes.BroughtForwardLossesController.broughtForwardLosses().url)
        ))
      case _ => None
    }
  }

}
