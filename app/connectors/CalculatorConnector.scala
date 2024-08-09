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

import common.TaxDates
import common.nonresident.Rebased
import config.ApplicationConfig
import constructors._
import models._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpReadsInstances.readFromJson
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculatorConnector @Inject()(val httpProvider: HttpClientV2Provider,
                                    val appConfig: ApplicationConfig,
                                    val servicesConfig: ServicesConfig)
                                   (implicit ec: ExecutionContext) {
  private val http = httpProvider.get()

  val serviceUrl: String = servicesConfig.baseUrl("capital-gains-calculator")

  def calculateTotalGain(totalGainAnswersModel: TotalGainAnswersModel)
                        (implicit hc: HeaderCarrier): Future[Option[TotalGainResultsModel]] =
    http.post(url"$serviceUrl/capital-gains-calculator/non-resident/calculate-total-gain")
      .withBody(Json.toJson(totalGainAnswersModel))
      .setHeader("Coontent-Type" -> "application/json")
      .execute[Option[TotalGainResultsModel]]

  def calculateTaxableGainAfterPRR(totalGainAnswersModel: TotalGainAnswersModel,
                                   privateResidenceReliefModel: PrivateResidenceReliefModel,
                                   propertyLivedInModel: PropertyLivedInModel)
                                  (implicit hc: HeaderCarrier): Future[Option[CalculationResultsWithPRRModel]] = {
    val totalGain = TotalGainRequestConstructor.totalGainQuery(totalGainAnswersModel)
    val prrRelief = PrivateResidenceReliefRequestConstructor.privateResidenceReliefQuery(
      Some(privateResidenceReliefModel),
      Some(propertyLivedInModel)).getOrElse(Map())
    http.get(url"$serviceUrl/capital-gains-calculator/non-resident/calculate-gain-after-prr?${totalGain ++ prrRelief}")
      .execute[Option[CalculationResultsWithPRRModel]]
  }

  def calculateNRCGTTotalTax(totalGainAnswersModel: TotalGainAnswersModel,
                             privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                             propertyLivedInModel: Option[PropertyLivedInModel],
                             totalTaxPersonalDetailsModel: Option[TotalPersonalDetailsCalculationModel],
                             maxAnnualExemptAmount: BigDecimal,
                             otherReliefs: Option[AllOtherReliefsModel] = None)(implicit hc: HeaderCarrier):
  Future[Option[CalculationResultsWithTaxOwedModel]] = {
    val totalGain = TotalGainRequestConstructor.totalGainQuery(totalGainAnswersModel)
    val prrRelief = PrivateResidenceReliefRequestConstructor.privateResidenceReliefQuery(
      privateResidenceReliefModel,
      propertyLivedInModel)
    val additionalParams = FinalTaxAnswersRequestConstructor.additionalParametersQuery(totalTaxPersonalDetailsModel, maxAnnualExemptAmount)
    val otherReliefsQuery = OtherReliefsRequestConstructor.otherReliefsQuery(otherReliefs)
    val params = totalGain ++ additionalParams ++ otherReliefsQuery ++ prrRelief.getOrElse(Map())
    http.get(url"$serviceUrl/capital-gains-calculator/non-resident/calculate-tax-owed?$params")
      .execute[Option[CalculationResultsWithTaxOwedModel]]
  }

  def getFullAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.get(url"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear")
      .execute[Option[BigDecimal]]
  }

  def getPA(taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    val eligible = Some(isEligibleBlindPersonsAllowance).filter(identity)
    http.get(url"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=$taxYear&isEligibleBlindPersonsAllowance=$eligible")
      .execute[Option[BigDecimal]]
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
      val params =
        if (calculationElection.calculationType == Rebased) {
          Map(
            "disposalCosts" -> answers.disposalCostsModel.disposalCosts.toDouble,
            "acquisitionCosts" -> answers.rebasedCostsModel.get.rebasedCosts.map(_.toDouble).getOrElse(0.0),
            "improvements" -> answers.improvementsModel.flatMap(_.improvementsAmtAfter).map(_.toDouble).getOrElse(0.0),
          )
        } else {
          Map(
            "disposalCosts" -> answers.disposalCostsModel.disposalCosts.toDouble,
            "acquisitionCosts" -> selectAcquisitionCosts(answers).toDouble,
            "improvements" ->
              (answers.improvementsModel match {
                case Some(model) => model.improvementsAmt.toDouble + model.improvementsAmtAfter.map(_.toDouble).getOrElse(0.0)
                case None => 0.0
              })
          )
        }
      http.get(url"$serviceUrl/capital-gains-calculator/non-resident/calculate-total-costs?$params")
        .execute[BigDecimal]
    case _ => Future.successful(throw new Exception("No calculation election supplied"))
  }

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    http.get(url"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear").execute[Option[TaxYearModel]]
  }
}
