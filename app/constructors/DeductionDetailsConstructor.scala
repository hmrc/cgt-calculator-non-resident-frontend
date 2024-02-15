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

import common.KeystoreKeys.{NonResidentKeys => keys}
import common.{Dates, TaxDates}
import models._

object DeductionDetailsConstructor {

  def datesOutsideRangeCheck(acquisitionDateModel: DateModel, disposalDateModel: DateModel): Boolean = {

    val pRRDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(disposalDateModel)

    acquisitionDateModel.get.plusMonths(pRRDateDetails.months).isBefore(disposalDateModel.get)
  }

  def acquisitionAfterPropertyDisposalOver18Month(acquisitionDateModel: DateModel, disposalDateModel: DateModel): Boolean = {

    val pRRDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(disposalDateModel)

    acquisitionDateModel.get.isAfter(TaxDates.taxStartDate) && acquisitionDateModel.get.plusMonths(pRRDateDetails.months).isBefore(disposalDateModel.get)
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
        "calc.propertyLivedIn.title",
        Some(controllers.routes.PropertyLivedInController.propertyLivedIn.url)
      ))
      case _ => Seq()
    }
  }

  def privateResidenceReliefQuestionRow(prr: Option[PrivateResidenceReliefModel]): Option[QuestionAnswerModel[String]] = {
    prr match {
      case Some(PrivateResidenceReliefModel(answer, _, _)) => Some(QuestionAnswerModel(
        keys.privateResidenceRelief,
        answer,
        "calc.privateResidenceRelief.question",
        Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url)
      ))
      case _ => None
    }
  }

  def privateResidenceReliefDaysClaimedBeforeRow(prr: Option[PrivateResidenceReliefModel],
                                                 answers: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {
    val datesOutside = datesOutsideRangeCheck(answers.acquisitionDateModel, answers.disposalDateModel)
    val acquisitionPostTaxStartDisposalPost18Month = acquisitionAfterPropertyDisposalOver18Month(answers.acquisitionDateModel, answers.disposalDateModel)
    (prr, datesOutside, acquisitionPostTaxStartDisposalPost18Month) match {
      case (Some(PrivateResidenceReliefModel("Yes", Some(value), _)), true, false) =>
        Some(QuestionAnswerModel(
          s"${keys.privateResidenceRelief}-daysClaimed",
          value.toString(),
          "calc.privateResidenceRelief.firstQuestion",
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url)
        ))
      case (Some(PrivateResidenceReliefModel("Yes", Some(value), _)), _, true) =>
        val pRRDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(answers.disposalDateModel)
        Some(QuestionAnswerModel(
          s"${keys.privateResidenceRelief}-daysClaimed",
          value.toString(),
          "calc.privateResidenceRelief.questionFlat",
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url),
          Dates.dateMinusMonths(answers.disposalDateModel, pRRDateDetails.months)))
      case _ => None
    }
  }

  def privateResidenceReliefDaysClaimedAfterRow(prr: Option[PrivateResidenceReliefModel],
                                                answers: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {

    val pRRDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(answers.disposalDateModel)

    (prr, answers.rebasedValueModel) match {
      case (Some(PrivateResidenceReliefModel("Yes", _, Some(value))), _)
      if !TaxDates.dateAfterStart(answers.acquisitionDateModel.get) && TaxDates.dateAfterOctober(answers.disposalDateModel.get) =>
        Some(QuestionAnswerModel(
          s"${keys.privateResidenceRelief}-daysClaimedAfter",
          value.toString(),
         "calc.privateResidenceRelief.questionBetween",
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url),
          Dates.dateMinusMonths(answers.disposalDateModel, pRRDateDetails.months)
        ))
      case (Some(PrivateResidenceReliefModel("Yes", _, Some(value))), Some(_))
      if TaxDates.dateAfterOctober(answers.disposalDateModel.get) =>
        Some(QuestionAnswerModel(
          s"${keys.privateResidenceRelief}-daysClaimedAfter",
          value.toString(),
          "calc.privateResidenceRelief.questionBetween",
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url),
          Dates.dateMinusMonths(answers.disposalDateModel, pRRDateDetails.months)
        ))
      case _ => None
    }
  }
}
