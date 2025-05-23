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

package controllers

import common.DefaultRoutes._
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TaxDates
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.AnnualExemptAmountForm._
import models._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.annualExemptAmount

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AnnualExemptAmountController @Inject()(calcConnector: CalculatorConnector,
                                             sessionCacheService: SessionCacheService,
                                             mcc: MessagesControllerComponents,
                                             annualExemptAmountView: annualExemptAmount)(implicit ec: ExecutionContext)
                                              extends FrontendController(mcc) with ValidActiveSession with I18nSupport {


  private def fetchAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    calcConnector.getFullAEA(taxYear)
  }

  private def fetchAnnualExemptAmount(implicit request: Request[?]): Future[Option[AnnualExemptAmountModel]] = {
    sessionCacheService.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount)
  }

  private def fetchPreviousGainOrLoss(implicit request: Request[?]): Future[Option[PreviousLossOrGainModel]] = {
    sessionCacheService.fetchAndGetFormData[PreviousLossOrGainModel](KeystoreKeys.previousLossOrGain)
  }

  private def fetchPreviousLossAmount(gainOrLoss: String)(implicit request: Request[?]): Future[Option[HowMuchLossModel]] = {
    gainOrLoss match {
      case "Loss" => sessionCacheService.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss)
      case _ => Future.successful(None)
    }
  }

  private def fetchPreviousGainAmount(gainOrLoss: String)(implicit request: Request[?]): Future[Option[HowMuchGainModel]] = {
    gainOrLoss match {
      case "Gain" => sessionCacheService.fetchAndGetFormData[HowMuchGainModel](KeystoreKeys.howMuchGain)
      case _ => Future.successful(None)
    }
  }

  private def fetchDisposalDate(implicit request: Request[?]): Future[Option[DateModel]] = {
    sessionCacheService.fetchAndGetFormData[DateModel](KeystoreKeys.disposalDate)
  }

  private def backUrl(lossOrGain: PreviousLossOrGainModel, gainAmount: Option[HowMuchGainModel], lossAmount: Option[HowMuchLossModel]): Future[String] = {
    (lossOrGain, gainAmount, lossAmount) match {
      case (PreviousLossOrGainModel("Neither"), None, None) =>
        Future.successful(routes.PreviousGainOrLossController.previousGainOrLoss.url)
      case (PreviousLossOrGainModel("Gain"), Some(data), None) if data.howMuchGain == 0 =>
        Future.successful(routes.HowMuchGainController.howMuchGain.url)
      case (PreviousLossOrGainModel("Loss"), None, Some(data)) if data.loss == 0 =>
        Future.successful(routes.HowMuchLossController.howMuchLoss.url)
      case _ => Future.successful(missingDataRoute)
    }
  }

  private def formatDisposalDate(disposalDateModel: Option[DateModel]): Future[String] = {
    val date = disposalDateModel.get
    Future.successful(s"${date.year}-${date.month}-${date.day}")
  }

  private def getTaxYear(details: Option[TaxYearModel]) = {
    val taxYear = details.get
    Future.successful(TaxDates.taxYearStringToInteger(taxYear.calculationTaxYear))
  }

  val annualExemptAmount: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[AnnualExemptAmountModel], maxAEA: BigDecimal, backUrl: String): Future[Result] = {
      sessionCacheService.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount).map {
        case Some(data) => Ok(annualExemptAmountView(annualExemptAmountForm().fill(data), maxAEA, backUrl))
        case None => Ok(annualExemptAmountView(annualExemptAmountForm(), maxAEA, backUrl))
      }
    }

    (for {
      disposalDate <- fetchDisposalDate(using request)
      date <- formatDisposalDate(disposalDate)
      closestTaxYear <- calcConnector.getTaxYear(date)
      taxYear <- getTaxYear(closestTaxYear)
      maxAEA <- fetchAEA(taxYear)
      annualExemptAmount <- fetchAnnualExemptAmount(using request)
      previousLossOrGain <- fetchPreviousGainOrLoss(using request)
      gainAmount <- fetchPreviousGainAmount(previousLossOrGain.get.previousLossOrGain)
      lossAmount <- fetchPreviousLossAmount(previousLossOrGain.get.previousLossOrGain)
      backUrl <- backUrl(previousLossOrGain.get, gainAmount, lossAmount)
      finalResult <- routeRequest(annualExemptAmount, maxAEA.get, backUrl)
    } yield finalResult).recoverToStart
  }

  val submitAnnualExemptAmount: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[AnnualExemptAmountModel], maxAEA: BigDecimal, backUrl: String) = {
      Future.successful(BadRequest(annualExemptAmountView(form, maxAEA, backUrl)))
    }

    def successAction(model: AnnualExemptAmountModel) = {
      sessionCacheService.saveFormData(KeystoreKeys.annualExemptAmount, model).map(_ =>
        Redirect(routes.BroughtForwardLossesController.broughtForwardLosses))
    }

    def routeRequest(maxAEA: BigDecimal, backUrl: String): Future[Result] = {
      annualExemptAmountForm(maxAEA).bindFromRequest().fold(
        errors => errorAction(errors, maxAEA, backUrl),
        success => successAction(success))
    }

    (for {
      disposalDate <- fetchDisposalDate(using request)
      date <- formatDisposalDate(disposalDate)
      closestTaxYear <- calcConnector.getTaxYear(date)
      taxYear <- getTaxYear(closestTaxYear)
      maxAEA <- fetchAEA(taxYear)
      previousLossOrGain <- fetchPreviousGainOrLoss(using request)
      gainAmount <- fetchPreviousGainAmount(previousLossOrGain.get.previousLossOrGain)
      lossAmount <- fetchPreviousLossAmount(previousLossOrGain.get.previousLossOrGain)
      backUrl <- backUrl(previousLossOrGain.get, gainAmount, lossAmount)
      finalResult <- routeRequest(maxAEA.get, backUrl)
    } yield finalResult).recoverToStart
  }
}
