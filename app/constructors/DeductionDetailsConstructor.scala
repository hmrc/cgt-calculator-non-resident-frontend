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
import common.TaxDates
import models._

object DeductionDetailsConstructor {

  private def datesOutsideRangeCheck(acquisitionDateModel: DateModel, disposalDateModel: DateModel): Boolean = {

    val pRRDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(disposalDateModel)

    acquisitionDateModel.get.plusMonths(pRRDateDetails.months).isBefore(disposalDateModel.get)
  }

  private def acquisitionAfterPropertyDisposalOver18Month(acquisitionDateModel: DateModel, disposalDateModel: DateModel): Boolean = {

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
        val privateResidenceReliefAmount = privateResidenceReliefAmountRow(privateResidenceReliefModel, answers)

        val sequence = Seq(
          privateResidenceReliefQuestion,
          privateResidenceReliefAmount)

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
      case Some(PrivateResidenceReliefModel(answer, _)) => Some(QuestionAnswerModel(
        keys.privateResidenceRelief,
        answer,
        "calc.privateResidenceRelief.question",
        Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url)
      ))
      case _ => None
    }
  }

  def privateResidenceReliefAmountRow(prr: Option[PrivateResidenceReliefModel],
                                      answers: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {
    val datesOutside = datesOutsideRangeCheck(answers.acquisitionDateModel, answers.disposalDateModel)
    val acquisitionPostTaxStartDisposalPost18Month = acquisitionAfterPropertyDisposalOver18Month(answers.acquisitionDateModel, answers.disposalDateModel)
    prr match {
      case Some(PrivateResidenceReliefModel("Yes", Some(value))) if datesOutside || acquisitionPostTaxStartDisposalPost18Month =>
        Some(QuestionAnswerModel(
          s"${keys.privateResidenceRelief}-prrClaimed",
          value,
          "calc.privateResidenceReliefValue.title",
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url)
        ))
      case _ => None
    }
  }
}
