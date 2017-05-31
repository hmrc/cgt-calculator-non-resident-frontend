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

package common.nonresident

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TaxDates
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import models._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TaxableGainCalculation {

  def getPRRResponse(totalGainResultsModel: TotalGainResultsModel,
                     calcConnector: CalculatorConnector)(implicit hc: HeaderCarrier):
  Future[Option[PrivateResidenceReliefModel]] = {
    val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
    val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

    if (finalSeq.exists(_ > 0)) {
      calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)
    } else Future(None)
  }

  def getPRRIfApplicable(totalGainAnswersModel: TotalGainAnswersModel,
                         privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                         calcConnector: CalculatorConnector)(implicit hc: HeaderCarrier):
  Future[Option[CalculationResultsWithPRRModel]] = {

    privateResidenceReliefModel match {
      case Some(data) => calcConnector.calculateTaxableGainAfterPRR(totalGainAnswersModel, data)
      case None => Future.successful(None)
    }
  }

  def getFinalSectionsAnswers(totalGainResultsModel: TotalGainResultsModel,
                              calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel],
                              calcConnector: CalculatorConnector,
                              answersConstructor: AnswersConstructor)(implicit hc: HeaderCarrier):
  Future[Option[TotalPersonalDetailsCalculationModel]] = {

    calculationResultsWithPRRModel match {

      case Some(data) =>
        val results = data.flatResult :: List(data.rebasedResult, data.timeApportionedResult).flatten

        if (results.exists(_.taxableGain > 0)) {
          answersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)
        } else Future(None)

      case None =>
        val gains = totalGainResultsModel.flatGain :: List(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten

        if (gains.exists(_ > 0)) {
          answersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)
        } else Future(None)
    }
  }

  def getMaxAEA(taxYear: Option[TaxYearModel],
                calcConnector: CalculatorConnector)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    calcConnector.getFullAEA(TaxDates.taxYearStringToInteger(taxYear.get.calculationTaxYear))
  }

  def getTaxYear(totalGainAnswersModel: TotalGainAnswersModel, calcConnector: CalculatorConnector)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    val date = totalGainAnswersModel.disposalDateModel
    calcConnector.getTaxYear(s"${date.year}-${date.month}-${date.day}")
  }

  def getChargeableGain(totalGainAnswersModel: TotalGainAnswersModel,
                        prrModel: Option[PrivateResidenceReliefModel],
                        personalDetailsModel: Option[TotalPersonalDetailsCalculationModel],
                        maxAEA: BigDecimal,
                        calcConnector: CalculatorConnector)(implicit hc: HeaderCarrier): Future[Option[CalculationResultsWithTaxOwedModel]] = {

    personalDetailsModel match {
      case Some(data) => calcConnector.calculateNRCGTTotalTax(totalGainAnswersModel, prrModel, personalDetailsModel, maxAEA)
      case None => Future(None)
    }
  }
}
