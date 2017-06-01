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

import models._
import common.{Dates, TaxDates}
import common.KeystoreKeys.{NonResidentKeys => keys}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object DeductionDetailsConstructor {

  def datesNotValidCheck(acquisitionDateModel: AcquisitionDateModel, disposalDateModel: DisposalDateModel): Boolean = {
    acquisitionDateModel.get.plusMonths(18).isBefore(disposalDateModel.get)
  }

  def deductionDetailsRows(answers: TotalGainAnswersModel,
                           privateResidenceReliefModel: Option[PrivateResidenceReliefModel] = None,
                           livedIn: Option[PropertyLivedInModel]): Seq[QuestionAnswerModel[Any]] = {

    val propertyLivedInRow = propertyLivedInQuestionRow(livedIn)

    if(!propertyLivedInRow.isEmpty) {

      val privateResidenceReliefQuestion = privateResidenceReliefQuestionRow(privateResidenceReliefModel)
      val privateResidenceReliefDaysClaimed = privateResidenceReliefDaysClaimedRow(privateResidenceReliefModel, answers)
      val privateResidenceReliefDaysClaimedAfter = privateResidenceReliefDaysClaimedAfterRow(privateResidenceReliefModel, answers)

      val sequence = Seq(
        privateResidenceReliefQuestion,
        privateResidenceReliefDaysClaimed,
        privateResidenceReliefDaysClaimedAfter)

      propertyLivedInRow ++ sequence.flatten
    }

    else {
      propertyLivedInRow
    }
  }

  def propertyLivedInQuestionRow(livedIn: Option[PropertyLivedInModel]): Seq[QuestionAnswerModel[String]] = {
    livedIn match {
      case Some(PropertyLivedInModel(true)) => Seq(QuestionAnswerModel(
        keys.propertyLivedIn,
        "Lived in property",
        Messages("calc.propertyLivedIn.title"),
        Some(controllers.routes.PropertyLivedInController.propertyLivedIn().url)
      ))
      case _ => Seq()
    }
  }

  def privateResidenceReliefQuestionRow(prr: Option[PrivateResidenceReliefModel]): Option[QuestionAnswerModel[String]] = {
    prr match {
      case Some(PrivateResidenceReliefModel(answer, _, _)) => Some(QuestionAnswerModel(
        keys.privateResidenceRelief,
        answer,
        Messages("calc.privateResidenceRelief.question"),
        Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief().url)
      ))
      case _ => None
    }
  }

  def privateResidenceReliefDaysClaimedRow(prr: Option[PrivateResidenceReliefModel],
                                           answers: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {
    prr match {
      case Some(PrivateResidenceReliefModel("Yes", Some(value), _)) if datesNotValidCheck(answers.acquisitionDateModel, answers.disposalDateModel) =>
        Some(QuestionAnswerModel(
          s"${keys.privateResidenceRelief}-daysClaimed",
          value.toString(),
          s"${Messages("calc.privateResidenceRelief.questionBefore.partOne")} ${Dates.dateMinusMonths(answers.disposalDateModel, 18)}" +
            s" ${Messages("calc.privateResidenceRelief.questionBefore.partTwo")}",
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief().url)
        ))
      case _ => None
    }
  }

  def privateResidenceReliefDaysClaimedAfterRow(prr: Option[PrivateResidenceReliefModel],
                                                answers: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {
    (prr, answers.rebasedValueModel) match {
      case (Some(PrivateResidenceReliefModel("Yes", _, Some(value))), _)
      if !TaxDates.dateAfterStart(answers.acquisitionDateModel.get) && TaxDates.dateAfterOctober(answers.disposalDateModel.get) =>
        Some(QuestionAnswerModel(
          s"${keys.privateResidenceRelief}-daysClaimedAfter",
          value.toString(),
          s"${Messages("calc.privateResidenceRelief.questionBetween.partOne")} ${Dates.dateMinusMonths(answers.disposalDateModel, 18)}" +
            s" ${Messages("calc.privateResidenceRelief.questionBetween.partTwo")}",
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief().url)
        ))
      case (Some(PrivateResidenceReliefModel("Yes", _, Some(value))), Some(RebasedValueModel(Some(_))))
      if TaxDates.dateAfterOctober(answers.disposalDateModel.get) =>
        Some(QuestionAnswerModel(
          s"${keys.privateResidenceRelief}-daysClaimedAfter",
          value.toString(),
          s"${Messages("calc.privateResidenceRelief.questionBetween.partOne")} ${Dates.dateMinusMonths(answers.disposalDateModel, 18)}" +
            s" ${Messages("calc.privateResidenceRelief.questionBetween.partTwo")}",
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief().url)
        ))
      case _ => None
    }
  }
}
