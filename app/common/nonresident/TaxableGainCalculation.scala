/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

object TaxableGainCalculation {

  def checkGainExists(totalGainResultsModel: TotalGainResultsModel): Future[Boolean]= {
    val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
    val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

    Future.successful(finalSeq.exists(_ > 0))
  }

  def getPropertyLivedInResponse(gainExists: Boolean, calcConnector: CalculatorConnector)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[PropertyLivedInModel]] = {
    if (gainExists) {
      calcConnector.fetchAndGetFormData[PropertyLivedInModel](KeystoreKeys.propertyLivedIn)
    } else Future(None)
  }

  def getPrrResponse(propertyLivedInResponse: Option[PropertyLivedInModel],
                     calcConnector: CalculatorConnector)(implicit hc: HeaderCarrier, ec: ExecutionContext):
  Future[Option[PrivateResidenceReliefModel]] = {
    propertyLivedInResponse match {
      case Some(data) if data.propertyLivedIn =>
        calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)
      case _ => Future(None)
    }
  }

  def getPrrIfApplicable(totalGainAnswersModel: TotalGainAnswersModel,
                         privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                         propertyLivedInModel: Option[PropertyLivedInModel],
                         calcConnector: CalculatorConnector)(implicit hc: HeaderCarrier):
  Future[Option[CalculationResultsWithPRRModel]] = {

    (propertyLivedInModel, privateResidenceReliefModel) match {
      case (Some(propertyLivedIn), Some(prrModel)) =>
        calcConnector.calculateTaxableGainAfterPRR(totalGainAnswersModel,
          prrModel,
          propertyLivedIn)
      case _ => Future.successful(None)
    }
  }

  def getFinalSectionsAnswers(totalGainResultsModel: TotalGainResultsModel,
                              calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel],
                              calcConnector: CalculatorConnector,
                              answersConstructor: AnswersConstructor)(implicit hc: HeaderCarrier, ec: ExecutionContext):
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
                        propertyLivedInModel: Option[PropertyLivedInModel],
                        personalDetailsModel: Option[TotalPersonalDetailsCalculationModel],
                        maxAEA: BigDecimal,
                        calcConnector: CalculatorConnector)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[CalculationResultsWithTaxOwedModel]] = {

    personalDetailsModel match {
      case Some(_) => calcConnector.calculateNRCGTTotalTax(totalGainAnswersModel,
        prrModel,
        propertyLivedInModel,
        personalDetailsModel,
        maxAEA)
      case None => Future(None)
    }
  }
}
