/*
 * Copyright 2018 HM Revenue & Customs
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

import common.{TaxDates, YesNoKeys}
import common.nonresident.CalculationType
import config.{CalculatorSessionCache, WSHttp}
import constructors._
import models._
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.http.ws.{WSGet, WSPost}

object CalculatorConnector extends CalculatorConnector with ServicesConfig with WSHttp {
  override val sessionCache = CalculatorSessionCache
  override val http = new WSHttp with WSGet with WSPost
  override val serviceUrl = baseUrl("capital-gains-calculator")
}

trait CalculatorConnector {

  val sessionCache: SessionCache
  val http: HttpGet with HttpPost
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
      case _ => 0
    }
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
          s"&acquisitionCosts=${selectAcquisitionCosts(answers)}" +
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
