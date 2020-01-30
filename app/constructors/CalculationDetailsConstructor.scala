/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.routes
import models.{QuestionAnswerModel, TotalGainResultsModel}
import play.api.i18n.{Messages, MessagesProvider}
import common.nonresident.CalculationType
import javax.inject.Inject
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.MessagesActionBuilder

class CalculationDetailsConstructor @Inject()(implicit messagesProvider: MessagesProvider) {

  def buildSection(calculation: TotalGainResultsModel, calculationType: String): Seq[QuestionAnswerModel[Any]] = {
    val electionDetails = calculationElection(calculationType)
    val totalGainAmount = calculationType match {
      case CalculationType.flat => calculation.flatGain
      case CalculationType.timeApportioned => calculation.timeApportionedGain.get
      case CalculationType.rebased => calculation.rebasedGain.get
    }
    val totalGainDetails = totalGain(totalGainAmount)
    val totalLossDetails = totalLoss(totalGainAmount)
    Seq(
      electionDetails,
      totalGainDetails,
      totalLossDetails
    ).flatten
  }

  def calculationElection(calculationType: String): Option[QuestionAnswerModel[String]] = {

    val id = KeystoreKeys.calculationElection

    val question = Messages("calc.summary.calculation.details.calculationElection")

    val answer = calculationType match {
      case CalculationType.flat => Messages("calc.summary.calculation.details.flatCalculation")
      case CalculationType.timeApportioned => Messages("calc.summary.calculation.details.timeCalculation")
      case CalculationType.rebased => Messages("calc.summary.calculation.details.rebasedCalculation")
    }

    val link = routes.CalculationElectionController.calculationElection().url

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
