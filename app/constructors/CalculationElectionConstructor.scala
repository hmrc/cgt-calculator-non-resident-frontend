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

import javax.inject.Singleton
import models._
import scala.concurrent.Future


@Singleton
class DefaultCalculationElectionConstructor extends CalculationElectionConstructor

trait CalculationElectionConstructor {

  case class CalculationElectionOption[T](calcType: String,
                                          amount: BigDecimal,
                                          message: String,
                                          calcDescription: String,
                                          date: Option[String],
                                          data: T,
                                          otherReliefs: Option[BigDecimal])

  def generateElection(totalGain: TotalGainResultsModel,
                       totalGainWithPrr: Option[CalculationResultsWithPRRModel],
                       taxOwed: Option[CalculationResultsWithTaxOwedModel],
                       otherReliefs: Option[AllOtherReliefsModel]
                      ): Future[Seq[(String, String, String, String, Option[String], Option[BigDecimal])]] = {

    val electionOptions = (totalGain, totalGainWithPrr, taxOwed) match {
      case (_, _, Some(data)) => buildElectionWithTaxOwed(data, otherReliefs)
      case (_, Some(data), _) => buildElectionWithPrr(data, otherReliefs)
      case _ => buildElectionWithTotalGain(totalGain)
    }

    Future.successful(electionOptions)
  }

  private def buildElectionWithTotalGain(data: TotalGainResultsModel) = {
    val flatElement = Some(flatElementConstructor(0.0, data.flatGain, None))
    val rebasedElement = data.rebasedGain.collect { case totalGain => rebasedElementConstructor(0.0, totalGain, None) }
    val timeElement = data.timeApportionedGain.collect { case totalGain => timeElementConstructor(0.0, totalGain, None) }
    val options = Seq(flatElement, rebasedElement, timeElement).flatten

    options
      .sortBy(option => option.data)
      .map(o => (o.calcType, o.amount.toString(), o.message, o.calcDescription, o.date, o.otherReliefs))
  }

  private def buildElectionWithPrr(data: CalculationResultsWithPRRModel, otherReliefs: Option[AllOtherReliefsModel]) = {
    val flatElement = Some(flatElementConstructor(0.0, data.flatResult,
      Some(otherReliefs.flatMap(_.otherReliefsFlat.map(_.otherReliefs)).getOrElse(BigDecimal(0)))))
    val rebasedElement = data.rebasedResult.collect { case result => rebasedElementConstructor(0.0, result,
      Some(otherReliefs.flatMap(_.otherReliefsRebased.map(_.otherReliefs)).getOrElse(BigDecimal(0)))) }
    val timeElement = data.timeApportionedResult.collect { case result => timeElementConstructor(0.0, result,
      Some(otherReliefs.flatMap(_.otherReliefsTime.map(_.otherReliefs)).getOrElse(BigDecimal(0)))) }
    val options = Seq(flatElement, rebasedElement, timeElement).flatten

    options
      .sortBy(option => (option.data.totalGain, option.data.taxableGain))
      .map(o => (o.calcType, o.amount.toString(), o.message, o.calcDescription, o.date, o.otherReliefs))
  }

  private def buildElectionWithTaxOwed(data: CalculationResultsWithTaxOwedModel, otherReliefs: Option[AllOtherReliefsModel]) = {
    val flatElement = Some(flatElementConstructor(data.flatResult.taxOwed, data.flatResult,
      Some(otherReliefs.flatMap(_.otherReliefsFlat.map(_.otherReliefs)).getOrElse(BigDecimal(0)))))
    val rebasedElement = data.rebasedResult.collect { case result => rebasedElementConstructor(result.taxOwed, result,
      Some(otherReliefs.flatMap(_.otherReliefsRebased.map(_.otherReliefs)).getOrElse(BigDecimal(0)))) }
    val timeElement = data.timeApportionedResult.collect { case result => timeElementConstructor(result.taxOwed, result,
      Some(otherReliefs.flatMap(_.otherReliefsTime.map(_.otherReliefs)).getOrElse(BigDecimal(0)))) }
    val options = Seq(flatElement, rebasedElement, timeElement).flatten

    options
      .sortBy(option => (option.data.totalGain, option.data.taxableGain, option.data.taxOwed))
      .map(o => (o.calcType, o.amount.toString(), o.message, o.calcDescription, o.date, o.otherReliefs))
  }

  def rebasedElementConstructor[T](amount: BigDecimal, data: T, otherReliefs: Option[BigDecimal]): CalculationElectionOption[T] = {
    CalculationElectionOption(
      "rebased",
      amount.setScale(2),
      "calc.calculationElection.message.rebased",
      "calc.calculationElection.description.rebased",
      Some("calc.calculationElection.message.rebasedDate"),
      data,
      otherReliefs
    )
  }

  def flatElementConstructor[T](amount: BigDecimal, data: T, otherReliefs: Option[BigDecimal]): CalculationElectionOption[T] = {
    CalculationElectionOption(
      "flat",
      amount.setScale(2),
      "calc.calculationElection.message.flat",
      "calc.calculationElection.description.flat",
      None,
      data,
      otherReliefs
    )
  }

  def timeElementConstructor[T](amount: BigDecimal, data: T, otherReliefs: Option[BigDecimal]): CalculationElectionOption[T] = {
    CalculationElectionOption(
      "time",
      amount.setScale(2),
      "calc.calculationElection.message.time",
      "calc.calculationElection.description.time",
      Some("calc.calculationElection.message.timeDate"),
      data,
      otherReliefs
    )
  }
}
