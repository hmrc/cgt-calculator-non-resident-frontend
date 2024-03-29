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
import models.{QuestionAnswerModel, TotalGainAnswersModel}

object PropertyDetailsConstructor {

  def propertyDetailsRows(answers: TotalGainAnswersModel): Seq[QuestionAnswerModel[Any]] = {

    val showImprovements = answers.isClaimingImprovementsModel.isClaimingImprovements
    val showRebasedImprovements = showImprovements && !TaxDates.dateAfterStart(answers.acquisitionDateModel.get)

    val claimingImprovementsRow = constructClaimingImprovementsRow(answers)
    val improvementsTotalRow = constructTotalImprovementsRow(answers, showImprovements, showRebasedImprovements)
    val improvementsAfterRow = constructImprovementsAfterRow(answers, showImprovements, showRebasedImprovements)

    val sequence = Seq(claimingImprovementsRow, improvementsTotalRow, improvementsAfterRow)
    sequence.flatten
  }

  def constructClaimingImprovementsRow(answers: TotalGainAnswersModel): Option[QuestionAnswerModel[Boolean]] = {

    Some(QuestionAnswerModel[Boolean](keys.isClaimingImprovements,
      answers.isClaimingImprovementsModel.isClaimingImprovements,
      "calc.improvements.question",
      Some(controllers.routes.ImprovementsController.getIsClaimingImprovements.url)
    ))
  }

  def constructTotalImprovementsRow(answers: TotalGainAnswersModel, display: Boolean, displayRebased: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    if (display && displayRebased && answers.improvementsModel.isDefined) {
      Some(QuestionAnswerModel[BigDecimal](s"${keys.improvements}-total",
        answers.improvementsModel.get.improvementsAmt,
        "calc.improvements.questionThree",
        Some(controllers.routes.ImprovementsController.improvementsRebased.url)
      ))
    }
    else if(display && answers.improvementsModel.isDefined) {
      val total: BigDecimal = answers.improvementsModel.get.improvementsAmt
      Some(QuestionAnswerModel[BigDecimal](s"${keys.improvements}-total",
        total,
        "calc.improvements.questionTwo",
        Some(controllers.routes.ImprovementsController.improvements.url)
      ))
    }
    else None
  }

  def constructImprovementsAfterRow(answers: TotalGainAnswersModel, display: Boolean, displayRebased: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    if (display && displayRebased && answers.improvementsModel.isDefined) {
      Some(QuestionAnswerModel[BigDecimal](s"${keys.improvements}-after",
        answers.improvementsModel.get.improvementsAmtAfter.getOrElse(0),
        "calc.improvements.questionFour",
        Some(controllers.routes.ImprovementsController.improvementsRebased.url)
      ))
    }
    else None
  }
}
