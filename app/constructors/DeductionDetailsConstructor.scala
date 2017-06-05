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

  def datesOutsideRangeCheck(acquisitionDateModel: AcquisitionDateModel, disposalDateModel: DisposalDateModel): Boolean = {
    acquisitionDateModel.get.plusMonths(18).isBefore(disposalDateModel.get)
  }

  def acquisitionAfterPropertyDisposalOver18Month(acquisitionDateModel: AcquisitionDateModel, disposalDateModel: DisposalDateModel): Boolean = {
    //When acquisition date is after 6th April 2015,
    // and property is disposed more than 18 months later
    acquisitionDateModel.get.isAfter(TaxDates.taxStartDate) && acquisitionDateModel.get.plusMonths(18).isAfter(disposalDateModel.get)
  }

  def deductionDetailsRows(answers: TotalGainAnswersModel,
                           privateResidenceReliefModel: Option[PrivateResidenceReliefModel] = None,
                           livedIn: Option[PropertyLivedInModel]): Seq[QuestionAnswerModel[Any]] = {

    val propertyLivedInRow = propertyLivedInQuestionRow(livedIn)

    if(propertyLivedInRow.isEmpty) {

      Seq()

    }

    else {
      if(propertyLivedInRow.head.data == "No") {
        propertyLivedInRow
      }
      else {
        val privateResidenceReliefQuestion = privateResidenceReliefQuestionRow(privateResidenceReliefModel)
        val privateResidenceReliefDaysClaimedBefore = privateResidenceReliefDaysClaimedBeforeRow(privateResidenceReliefModel, answers)
        val privateResidenceReliefDaysClaimedAfter = privateResidenceReliefDaysClaimedAfterRow(privateResidenceReliefModel, answers)

        val sequence = Seq(
          privateResidenceReliefQuestion,
          privateResidenceReliefDaysClaimedBefore,
          privateResidenceReliefDaysClaimedAfter)

        propertyLivedInRow ++ sequence.flatten
      }
    }
  }

  def propertyLivedInQuestionRow(livedIn: Option[PropertyLivedInModel]): Seq[QuestionAnswerModel[String]] = {
    livedIn match {
      case Some(PropertyLivedInModel(answer)) => Seq(QuestionAnswerModel(
        keys.propertyLivedIn,
        if(answer) "Yes" else "No",
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

  def privateResidenceReliefDaysClaimedBeforeRow(prr: Option[PrivateResidenceReliefModel],
                                                 answers: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {
    val datesOutsideRangeCheck = datesOutsideRangeCheck(answers.acquisitionDateModel, answers.disposalDateModel)
    val acqusitionPostTaxStartDisposalPost18Month = acquisitionAfterPropertyDisposalOver18Month(answers.acquisitionDateModel, answers.disposalDateModel)

    (prr, datesOutsideRangeCheck, acqusitionPostTaxStartDisposalPost18Month) match {
      case (Some(PrivateResidenceReliefModel("Yes", Some(value), _)), true, false) =>
        Some(QuestionAnswerModel(
          s"${keys.privateResidenceRelief}-daysClaimed",
          value.toString(),
          Messages("calc.privateResidenceRelief.firstQuestion"),
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief().url)
        ))
      case (Some(PrivateResidenceReliefModel("Yes", Some(value), _)), _, true) =>
        Some(QuestionAnswerModel(
          s"${keys.privateResidenceRelief}-daysClaimed",
          value.toString(),
          Messages("EDGE MESSAGE"),
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief().url)
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
      case (Some(PrivateResidenceReliefModel("Yes", _, Some(value))), Some(_))
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
