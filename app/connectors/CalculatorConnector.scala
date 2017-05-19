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

package connectors

import common.YesNoKeys
import common.nonresident.CalculationType
import config.{CalculatorSessionCache, WSHttp}
import constructors._
import models._
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpResponse}

import scala.concurrent.Future

object CalculatorConnector extends CalculatorConnector with ServicesConfig {
  override val sessionCache = CalculatorSessionCache
  override val http = WSHttp
  override val serviceUrl = baseUrl("capital-gains-calculator")
}

trait CalculatorConnector {

  val sessionCache: SessionCache
  val http: HttpGet
  val serviceUrl: String

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

  def saveFormData[T](key: String, data: T)(implicit hc: HeaderCarrier, formats: Format[T]): Future[CacheMap] = {
    sessionCache.cache(key, data)
  }

  def fetchAndGetFormData[T](key: String)(implicit hc: HeaderCarrier, formats: Format[T]): Future[Option[T]] = {
    sessionCache.fetchAndGetEntry(key)
  }

  def calculateTotalGain(totalGainAnswersModel: TotalGainAnswersModel)
                        (implicit hc: HeaderCarrier): Future[Option[TotalGainResultsModel]] = {
    http.GET[Option[TotalGainResultsModel]](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-total-gain?${
      TotalGainRequestConstructor.totalGainQuery(totalGainAnswersModel)
    }")
  }

  def calculateTaxableGainAfterPRR(totalGainAnswersModel: TotalGainAnswersModel,
                                   privateResidenceReliefModel: PrivateResidenceReliefModel)
                                  (implicit hc: HeaderCarrier): Future[Option[CalculationResultsWithPRRModel]] = {
    http.GET[Option[CalculationResultsWithPRRModel]](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-gain-after-prr?${
      TotalGainRequestConstructor.totalGainQuery(totalGainAnswersModel) +
        PrivateResidenceReliefRequestConstructor.privateResidenceReliefQuery(totalGainAnswersModel, Some(privateResidenceReliefModel))
    }")
  }

  def calculateNRCGTTotalTax(totalGainAnswersModel: TotalGainAnswersModel,
                             privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                             totalTaxPersonalDetailsModel: TotalPersonalDetailsCalculationModel,
                             maxAnnualExemptAmount: BigDecimal,
                             otherReliefs: Option[AllOtherReliefsModel] = None)(implicit hc: HeaderCarrier):
  Future[Option[CalculationResultsWithTaxOwedModel]] = {

    http.GET[Option[CalculationResultsWithTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-tax-owed?${
      TotalGainRequestConstructor.totalGainQuery(totalGainAnswersModel) +
        PrivateResidenceReliefRequestConstructor.privateResidenceReliefQuery(totalGainAnswersModel, privateResidenceReliefModel) +
        FinalTaxAnswersRequestConstructor.additionalParametersQuery(totalTaxPersonalDetailsModel, maxAnnualExemptAmount) +
        OtherReliefsRequestConstructor.otherReliefsQuery(otherReliefs)
    }")
  }

  def calculateFlat(input: SummaryModel)(implicit hc: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    http.GET[Option[CalculationResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-flat?${
      CalculateRequestConstructor.baseCalcUrl(input)
    }${
      CalculateRequestConstructor.flatCalcUrlExtra(input)
    }")
  }

  def calculateTA(input: SummaryModel)(implicit hc: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    http.GET[Option[CalculationResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-time-apportioned?${
      CalculateRequestConstructor.baseCalcUrl(input)
    }${
      CalculateRequestConstructor.taCalcUrlExtra(input)
    }")
  }

  def calculateRebased(input: SummaryModel)(implicit hc: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    http.GET[Option[CalculationResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-rebased?${
      CalculateRequestConstructor.baseCalcUrl(input)
    }${
      CalculateRequestConstructor.rebasedCalcUrlExtra(input)
    }")
  }

  def getFullAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear")
  }

  def getPartialAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-partial-aea?taxYear=$taxYear")
  }

  def getPA(taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=$taxYear" +
      s"${
        if (isEligibleBlindPersonsAllowance) s"&isEligibleBlindPersonsAllowance=true"
        else ""
      }"
    )
  }

  def calculateTotalCosts(answers: TotalGainAnswersModel, calculationType: Option[CalculationElectionModel]): Future[BigDecimal] = calculationType match {
    case Some(calculationElection) =>
      if(calculationElection.calculationType == CalculationType.rebased) {
        http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-total-costs?" +
          s"disposalCosts=${answers.disposalCostsModel.disposalCosts}" +
          s"&acquisitionCosts=${answers.rebasedCostsModel.get.rebasedCosts.getOrElse(0)}" +
          improvementsQueryParameter(answers.improvementsModel, answers.improvementsModel.improvementsAmtAfter.getOrElse(0)))
      } else {
        http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-total-costs?" +
          s"disposalCosts=${answers.disposalCostsModel.disposalCosts}" +
          s"&acquisitionCosts=${answers.acquisitionCostsModel.acquisitionCostsAmt}" +
          improvementsQueryParameter(answers.improvementsModel,
            answers.improvementsModel.improvementsAmt.getOrElse(BigDecimal(0)) + answers.improvementsModel.improvementsAmtAfter.getOrElse(BigDecimal(0))))
      }
    case _ => Future.successful(throw new Exception("No calculation election supplied"))
  }

  private def improvementsQueryParameter(improvementsModel: ImprovementsModel, value: BigDecimal): String = {
    if(improvementsModel.isClaimingImprovements == YesNoKeys.yes) s"&improvements=$value"
    else "&improvements=0"
  }

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    http.GET[Option[TaxYearModel]](s"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear")
  }

  def clearKeystore(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    sessionCache.remove()
  }
}
