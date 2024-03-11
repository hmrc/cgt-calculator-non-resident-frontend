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

package connectors

import common.nonresident.Rebased
import common.TaxDates
import config.ApplicationConfig
import constructors._
import models._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculatorConnector @Inject()(val http: DefaultHttpClient,
                                    val appConfig: ApplicationConfig,
                                    val servicesConfig: ServicesConfig)
                                   (implicit ec: ExecutionContext) {

  val serviceUrl: String = servicesConfig.baseUrl("capital-gains-calculator")

  def calculateTotalGain(totalGainAnswersModel: TotalGainAnswersModel)
                        (implicit hc: HeaderCarrier): Future[Option[TotalGainResultsModel]] = {
    http.POST[TotalGainAnswersModel, Option[TotalGainResultsModel]](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-total-gain",
      totalGainAnswersModel)
  }

  def calculateTaxableGainAfterPRR(totalGainAnswersModel: TotalGainAnswersModel,
                                   privateResidenceReliefModel: PrivateResidenceReliefModel,
                                   propertyLivedInModel: PropertyLivedInModel)
                                  (implicit hc: HeaderCarrier): Future[Option[CalculationResultsWithPRRModel]] = {
    http.GET[Option[CalculationResultsWithPRRModel]](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-gain-after-prr?${
      TotalGainRequestConstructor.totalGainQuery(totalGainAnswersModel) +
        PrivateResidenceReliefRequestConstructor.privateResidenceReliefQuery(totalGainAnswersModel,
          Some(privateResidenceReliefModel),
          Some(propertyLivedInModel))
    }")
  }

  def calculateNRCGTTotalTax(totalGainAnswersModel: TotalGainAnswersModel,
                             privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                             propertyLivedInModel: Option[PropertyLivedInModel],
                             totalTaxPersonalDetailsModel: Option[TotalPersonalDetailsCalculationModel],
                             maxAnnualExemptAmount: BigDecimal,
                             otherReliefs: Option[AllOtherReliefsModel] = None)(implicit hc: HeaderCarrier):
  Future[Option[CalculationResultsWithTaxOwedModel]] = {

    http.GET[Option[CalculationResultsWithTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-tax-owed?${
      TotalGainRequestConstructor.totalGainQuery(totalGainAnswersModel) +
        PrivateResidenceReliefRequestConstructor.privateResidenceReliefQuery(totalGainAnswersModel,
          privateResidenceReliefModel,
          propertyLivedInModel) +
        FinalTaxAnswersRequestConstructor.additionalParametersQuery(totalTaxPersonalDetailsModel, maxAnnualExemptAmount) +
        OtherReliefsRequestConstructor.otherReliefsQuery(otherReliefs)
    }")
  }

  def getFullAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear")
  }

  def getPA(taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=$taxYear" +
      s"${
        if (isEligibleBlindPersonsAllowance) s"&isEligibleBlindPersonsAllowance=true"
        else ""
      }"
    )
  }

  private def selectAcquisitionCosts(answers: TotalGainAnswersModel) = {
    (answers.acquisitionCostsModel, answers.costsAtLegislationStart) match {
      case (_, Some(model)) if TaxDates.dateBeforeLegislationStart(answers.acquisitionDateModel.get) && model.hasCosts == "Yes" =>
        model.costs.get
      case (Some(model), _) if !TaxDates.dateBeforeLegislationStart(answers.acquisitionDateModel.get) =>
        model.acquisitionCostsAmt
      case _ => BigDecimal(0)
    }
  }

  def calculateTotalCosts(
                           answers: TotalGainAnswersModel,
                           calculationType: Option[CalculationElectionModel]
                         )(implicit hc: HeaderCarrier): Future[BigDecimal] = calculationType match {
    case Some(calculationElection) =>
      if(calculationElection.calculationType == Rebased) {
        http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-total-costs?" +
          s"disposalCosts=${answers.disposalCostsModel.disposalCosts.toDouble}" +
          s"&acquisitionCosts=${answers.rebasedCostsModel.get.rebasedCosts.getOrElse(BigDecimal(0)).toDouble}" +
          getImprovements(answers, rebased = true))
      } else {
        http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-total-costs?" +
          s"disposalCosts=${answers.disposalCostsModel.disposalCosts.toDouble}" +
          s"&acquisitionCosts=${selectAcquisitionCosts(answers).toDouble}" +
          getImprovements(answers, rebased = false))
      }
    case _ => Future.successful(throw new Exception("No calculation election supplied"))
  }

  private def getImprovements(answers: TotalGainAnswersModel, rebased: Boolean): String = {
    answers.improvementsModel match {
      case Some(model) => if(!rebased)
        s"&improvements=${model.improvementsAmt.toDouble + model.improvementsAmtAfter.getOrElse(BigDecimal(0)).toDouble}"
        else s"&improvements=${model.improvementsAmtAfter.getOrElse(BigDecimal(0)).toDouble}"
      case _ => "&improvements=0.0"
    }
  }

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    http.GET[Option[TaxYearModel]](s"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear")
  }
}
