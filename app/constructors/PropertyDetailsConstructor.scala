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

package constructors

import models.{AcquisitionDateModel, QuestionAnswerModel, TotalGainAnswersModel}
import common.TaxDates
import common.KeystoreKeys.{NonResidentKeys => keys}
import play.api.i18n.Messages

object PropertyDetailsConstructor {

  def propertyDetailsRows(answers: TotalGainAnswersModel): Seq[QuestionAnswerModel[Any]] = {

    val rebasedImprovements =
      answers.acquisitionDateModel match {
        case AcquisitionDateModel("Yes",_,_,_) if !TaxDates.dateAfterStart(answers.acquisitionDateModel.get) => true
        case AcquisitionDateModel("No",_,_,_) if answers.rebasedValueModel.get.rebasedValueAmt.isDefined => true
        case _ => false
      }

    val totalImprovements =
      if(answers.improvementsModel.isClaimingImprovements == "Yes") true
      else false

    val improvementsIsClaiming = improvementsIsClaimingRow(answers)
    val improvementsTotal = improvementsTotalRow(answers, totalImprovements, rebasedImprovements)
    val improvementsAfter = improvementsAfterRow(answers, rebasedImprovements)

    val sequence = Seq(improvementsIsClaiming, improvementsTotal, improvementsAfter)
    sequence.flatten
  }

  def improvementsIsClaimingRow(answers: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {
    Some(QuestionAnswerModel[String](s"${keys.improvements}-isClaiming",
      answers.improvementsModel.isClaimingImprovements,
      Messages("calc.improvements.question"),
      Some(controllers.routes.ImprovementsController.improvements().url)
    ))
  }

  def improvementsTotalRow(answers: TotalGainAnswersModel, display: Boolean, displayRebased: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    if (display && displayRebased) {
      val total: BigDecimal = answers.improvementsModel.improvementsAmt.getOrElse(BigDecimal(0))
      Some(QuestionAnswerModel[BigDecimal](s"${keys.improvements}-total",
        total,
        Messages("calc.improvements.questionThree"),
        Some(controllers.routes.ImprovementsController.improvements().url)
      ))
    }
    else if(display) {
      val total: BigDecimal = answers.improvementsModel.improvementsAmt.getOrElse(BigDecimal(0))
      Some(QuestionAnswerModel[BigDecimal](s"${keys.improvements}-total",
        total,
        Messages("calc.improvements.questionTwo"),
        Some(controllers.routes.ImprovementsController.improvements().url)
      ))
    }
    else None
  }

  def improvementsAfterRow(answers: TotalGainAnswersModel, display: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    if (display) {
      Some(QuestionAnswerModel[BigDecimal](s"${keys.improvements}-after",
        answers.improvementsModel.improvementsAmtAfter.getOrElse(0),
        Messages("calc.improvements.questionFour"),
        Some(controllers.routes.ImprovementsController.improvements().url)
      ))
    }
    else None
  }
}


