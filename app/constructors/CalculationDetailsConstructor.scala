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
import common.nonresident.{CalculationType, Flat, Rebased, TimeApportioned}
import controllers.routes
import models.{QuestionAnswerModel, TotalGainResultsModel}
import play.api.i18n.{Messages, MessagesProvider}

import javax.inject.Inject


class CalculationDetailsConstructor @Inject()(implicit messagesProvider: MessagesProvider) {

  def buildSection(calculation: TotalGainResultsModel, calculationType: CalculationType): Seq[QuestionAnswerModel[Any]] = {
    val electionDetails = calculationElection(calculationType)
    val totalGainAmount = calculationType match {
      case Flat => calculation.flatGain
      case TimeApportioned => calculation.timeApportionedGain.get
      case Rebased => calculation.rebasedGain.get
    }
    val totalGainDetails = totalGain(totalGainAmount)
    val totalLossDetails = totalLoss(totalGainAmount)
    Seq(
      electionDetails,
      totalGainDetails,
      totalLossDetails
    ).flatten
  }

  def calculationElection(calculationType: CalculationType): Option[QuestionAnswerModel[String]] = {

    val id = KeystoreKeys.calculationElection

    val question = Messages("calc.summary.calculation.details.calculationElection")

    val answer = calculationType match {
      case Flat => Messages("calc.summary.calculation.details.flatCalculation")
      case TimeApportioned => Messages("calc.summary.calculation.details.timeCalculation")
      case Rebased => Messages("calc.summary.calculation.details.rebasedCalculation")
    }

    val link = routes.CalculationElectionController.calculationElection.url

    Some(QuestionAnswerModel(id, answer, question, Some(link)))
  }

  def totalLoss(totalGain: BigDecimal): Option[QuestionAnswerModel[BigDecimal]] = {
    if (totalGain >= BigDecimal(0)) None
    else {
      val id = "calcDetails:totalLoss"

      val question = Messages("calc.summary.calculation.details.totalLoss")

      val answer = totalGain.abs

      Some(QuestionAnswerModel(id, answer, question, None))
    }
  }

  def totalGain(totalGain: BigDecimal): Option[QuestionAnswerModel[BigDecimal]] = {
    if (totalGain < BigDecimal(0)) None
    else {
      val id = "calcDetails:totalGain"

      val question = Messages("calc.summary.calculation.details.totalGain")

      val answer = totalGain

      Some(QuestionAnswerModel(id, answer, question, None))
    }
  }
}
